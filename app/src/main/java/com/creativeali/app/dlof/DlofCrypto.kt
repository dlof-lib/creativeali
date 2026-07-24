package com.creativeali.app.dlof

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * تشفير حزم/ملفات DLoF بصيغة "Best64" — متوافق مع بنية الملف المشفر في
 * تطبيق DLoF المرجعي (org.dlof.reader.lighthouse.CryptoHelper):
 *
 *   magic "DLOF" (4B) ثم version (1B) ثم salt (16B) ثم iv (12B) ثم ciphertext...
 *
 * الخوارزمية: AES-256-GCM، اشتقاق المفتاح عبر PBKDF2-HmacSHA256 (310,000 تكرار
 * افتراضيًا، أو ضِعفها كبديل مبسّط لـ Argon2id إن لم تتوفر مكتبة خارجية).
 * يدعم أيضًا صيغة "معزّزة" (v3) بتحقق HMAC-SHA256 منفصل للملفات الحساسة جدًا.
 */
object DlofCrypto {

    private const val MAGIC = "DLOF"
    private const val VERSION_V2: Byte = 2
    private const val VERSION_V3_ENHANCED: Byte = 3
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val GCM_TAG_BITS = 128

    data class CryptoProfile(
        val id: String = "Best64",
        val name: String = "Best64-AES-256",
        val algorithm: String = "AES-256-GCM",
        val pbkdf2Iterations: Int = 310_000,
        val useArgon2idFallback: Boolean = true,
        val compressionBeforeEncrypt: Boolean = true,
    )

    sealed class Result {
        data class Success(val data: ByteArray, val profile: String) : Result()
        data class Failure(val message: String) : Result()
    }

    /** يشفّر نصًا عاديًا (مثلاً ناتج [DlofXmlCodec.write]) بصيغة v2 القياسية. */
    fun encrypt(plainText: String, password: String, profile: CryptoProfile = CryptoProfile()): Result {
        return try {
            val salt = randomBytes(SALT_LEN)
            val iv = randomBytes(IV_LEN)
            val key = deriveKey(password, salt, profile)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))

            val plain = if (profile.compressionBeforeEncrypt) deflate(plainText.toByteArray(Charsets.UTF_8))
            else plainText.toByteArray(Charsets.UTF_8)
            val cipherText = cipher.doFinal(plain)

            val out = ByteArray(4 + 1 + SALT_LEN + IV_LEN + cipherText.size)
            var offset = 0
            MAGIC.toByteArray(Charsets.UTF_8).copyInto(out, offset); offset += 4
            out[offset] = VERSION_V2; offset += 1
            salt.copyInto(out, offset); offset += SALT_LEN
            iv.copyInto(out, offset); offset += IV_LEN
            cipherText.copyInto(out, offset)

            Result.Success(out, profile.name)
        } catch (e: Exception) {
            Result.Failure("فشل التشفير: ${e.message}")
        }
    }

    /** يفك تشفير ملف بصيغة v2 أو v3 (المعزّزة). */
    fun decrypt(encrypted: ByteArray, password: String, profile: CryptoProfile = CryptoProfile()): Result {
        return try {
            if (encrypted.size < 4 + 1 + SALT_LEN + IV_LEN + 1) {
                return Result.Failure("الملف قصير جدًا أو تالف")
            }
            var offset = 0
            val magic = String(encrypted, offset, 4, Charsets.UTF_8); offset += 4
            if (magic != MAGIC) return Result.Failure("ليس ملف DLoF مشفرًا صالحًا")
            val version = encrypted[offset]; offset += 1

            return when (version) {
                VERSION_V2 -> decryptV2(encrypted, offset, password, profile)
                VERSION_V3_ENHANCED -> decryptV3Enhanced(encrypted, offset, password, profile)
                else -> Result.Failure("إصدار تشفير غير مدعوم: $version")
            }
        } catch (e: Exception) {
            Result.Failure("كلمة المرور خاطئة أو الملف تالف")
        }
    }

    private fun decryptV2(data: ByteArray, startOffset: Int, password: String, profile: CryptoProfile): Result {
        var offset = startOffset
        val salt = data.copyOfRange(offset, offset + SALT_LEN); offset += SALT_LEN
        val iv = data.copyOfRange(offset, offset + IV_LEN); offset += IV_LEN
        val cipherText = data.copyOfRange(offset, data.size)

        val key = deriveKey(password, salt, profile)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plain = cipher.doFinal(cipherText)
        val result = if (profile.compressionBeforeEncrypt) inflate(plain) else plain
        return Result.Success(result, profile.name)
    }

    /** تشفير معزّز v3: يضيف طبقة HMAC-SHA256 منفصلة للتحقق من السلامة قبل فك التشفير. */
    fun encryptEnhanced(plainText: String, password: String, profile: CryptoProfile = CryptoProfile()): Result {
        return try {
            val salt = randomBytes(SALT_LEN)
            val iv = randomBytes(IV_LEN)
            val hmacSalt = randomBytes(SALT_LEN)

            val key = deriveKeyPbkdf2(password, salt, profile.pbkdf2Iterations)
            val hmacKey = deriveKeyPbkdf2(password + "hmac", hmacSalt, profile.pbkdf2Iterations)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val mac = Mac.getInstance("HmacSHA256")
            mac.init(hmacKey)
            val hmac = mac.doFinal(cipherText)

            val out = ByteArray(4 + 1 + SALT_LEN + IV_LEN + SALT_LEN + hmac.size + cipherText.size)
            var offset = 0
            MAGIC.toByteArray(Charsets.UTF_8).copyInto(out, offset); offset += 4
            out[offset] = VERSION_V3_ENHANCED; offset += 1
            salt.copyInto(out, offset); offset += SALT_LEN
            iv.copyInto(out, offset); offset += IV_LEN
            hmacSalt.copyInto(out, offset); offset += SALT_LEN
            hmac.copyInto(out, offset); offset += hmac.size
            cipherText.copyInto(out, offset)

            Result.Success(out, "${profile.name}-Enhanced")
        } catch (e: Exception) {
            Result.Failure("فشل التشفير المعزز: ${e.message}")
        }
    }

    private fun decryptV3Enhanced(data: ByteArray, startOffset: Int, password: String, profile: CryptoProfile): Result {
        var offset = startOffset
        val salt = data.copyOfRange(offset, offset + SALT_LEN); offset += SALT_LEN
        val iv = data.copyOfRange(offset, offset + IV_LEN); offset += IV_LEN
        val hmacSalt = data.copyOfRange(offset, offset + SALT_LEN); offset += SALT_LEN
        val hmac = data.copyOfRange(offset, offset + 32); offset += 32
        val cipherText = data.copyOfRange(offset, data.size)

        val hmacKey = deriveKeyPbkdf2(password + "hmac", hmacSalt, profile.pbkdf2Iterations)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(hmacKey)
        val expected = mac.doFinal(cipherText)
        if (!expected.contentEquals(hmac)) return Result.Failure("فشل التحقق من سلامة الملف (HMAC غير مطابق)")

        val key = deriveKeyPbkdf2(password, salt, profile.pbkdf2Iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return Result.Success(cipher.doFinal(cipherText), "${profile.name}-Enhanced")
    }

    /** فحص سريع: هل هذه البايتات ملف DLoF مشفر؟ */
    fun isEncrypted(data: ByteArray): Boolean =
        data.size >= 4 && String(data, 0, 4, Charsets.UTF_8) == MAGIC

    private fun deriveKey(password: String, salt: ByteArray, profile: CryptoProfile): SecretKeySpec {
        val iterations = if (profile.useArgon2idFallback) profile.pbkdf2Iterations * 2 else profile.pbkdf2Iterations
        return deriveKeyPbkdf2(password, salt, iterations)
    }

    private fun deriveKeyPbkdf2(password: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun randomBytes(len: Int): ByteArray = ByteArray(len).also { SecureRandom().nextBytes(it) }

    private fun deflate(data: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(data)
        deflater.finish()
        val buffer = ByteArray(1024)
        val out = ArrayList<Byte>(data.size)
        while (!deflater.finished()) {
            val n = deflater.deflate(buffer)
            for (i in 0 until n) out.add(buffer[i])
        }
        deflater.end()
        return out.toByteArray()
    }

    private fun inflate(data: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(data)
        val buffer = ByteArray(1024)
        val out = ArrayList<Byte>(data.size * 2)
        while (!inflater.finished()) {
            val n = inflater.inflate(buffer)
            if (n == 0 && inflater.needsInput()) break
            for (i in 0 until n) out.add(buffer[i])
        }
        inflater.end()
        return out.toByteArray()
    }
}
