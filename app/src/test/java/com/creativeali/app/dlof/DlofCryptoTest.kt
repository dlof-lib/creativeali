package com.creativeali.app.dlof

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DlofCryptoTest {

    @Test
    fun `encrypts and decrypts round trip with correct password`() {
        val plain = "<dlof>سر</dlof>"
        val enc = DlofCrypto.encrypt(plain, "correct-horse-battery-staple") as DlofCrypto.Result.Success
        assertTrue(DlofCrypto.isEncrypted(enc.data))

        val dec = DlofCrypto.decrypt(enc.data, "correct-horse-battery-staple") as DlofCrypto.Result.Success
        assertEquals(plain, String(dec.data, Charsets.UTF_8))
    }

    @Test
    fun `fails to decrypt with wrong password`() {
        val enc = DlofCrypto.encrypt("secret content", "right-password") as DlofCrypto.Result.Success
        val dec = DlofCrypto.decrypt(enc.data, "wrong-password")
        assertTrue(dec is DlofCrypto.Result.Failure)
    }

    @Test
    fun `enhanced encryption detects tampering via hmac`() {
        val enc = DlofCrypto.encryptEnhanced("important data", "pw123456") as DlofCrypto.Result.Success
        val tampered = enc.data.copyOf()
        tampered[tampered.size - 1] = (tampered[tampered.size - 1] + 1).toByte()

        val dec = DlofCrypto.decrypt(tampered, "pw123456")
        assertTrue(dec is DlofCrypto.Result.Failure)
    }

    @Test
    fun `non-dlof bytes are not reported as encrypted`() {
        assertFalse(DlofCrypto.isEncrypted("plain text".toByteArray()))
    }
}
