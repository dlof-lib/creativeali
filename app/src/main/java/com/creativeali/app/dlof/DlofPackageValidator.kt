package com.creativeali.app.dlof

import java.io.File

/**
 * يفحص هيكل حزمة `.dlofpkg` (بعد فك الضغط إلى مجلد مؤقت) ويُعيد تقريرًا
 * مفصلًا: ما المفقود، ما التحذيرات، وملخص الوسائط والتشفير.
 * مطابق لمنطق DocumentFileHelper (df.kt) في تطبيق DLoF المرجعي.
 */
object DlofPackageValidator {

    private val REQUIRED_FILES = listOf("set.txt")

    private val RECOMMENDED_FOLDERS = listOf(
        "media", "media/image", "media/video", "media/fonts", "setting", "setting/pro"
    )

    data class PackageCheck(
        val isValid: Boolean,
        val missingRequired: List<String>,
        val missingOptional: List<String>,
        val warnings: List<String>,
        val structure: PackageStructure?,
    )

    data class PackageStructure(
        val hasSetTxt: Boolean,
        val hasTemplate: Boolean,
        val hasCryptoProfile: Boolean,
        val hasDocumentation: Boolean,
        val hasLicense: Boolean,
        val media: MediaSummary,
        val cryptoEnabled: Boolean,
    )

    data class MediaSummary(
        val imageCount: Int, val videoCount: Int, val fontCount: Int,
        val hasChapters: Boolean, val hasEpisodes: Boolean,
    )

    fun checkPackage(pkgRoot: File): PackageCheck {
        val warnings = mutableListOf<String>()
        val missingRequired = mutableListOf<String>()
        val missingOptional = mutableListOf<String>()

        REQUIRED_FILES.forEach { f ->
            if (!File(pkgRoot, f).exists()) missingRequired.add(f)
        }
        RECOMMENDED_FOLDERS.forEach { f ->
            if (!File(pkgRoot, f).exists()) warnings.add("المجلد الموصى به مفقود: $f")
        }

        val optional = listOf(
            "setting/dlotemplate.xml" to "قالب XML",
            "setting/Documentation.dlof" to "التوثيق",
            "setting/license.dlof" to "الترخيص",
            "setting/pro/Best64.xml" to "إعدادات التشفير",
        )
        optional.forEach { (path, label) -> if (!File(pkgRoot, path).exists()) missingOptional.add(label) }

        val setFile = File(pkgRoot, "set.txt")
        val structure = if (setFile.exists()) parseStructure(pkgRoot, setFile) else null

        return PackageCheck(
            isValid = missingRequired.isEmpty(),
            missingRequired = missingRequired,
            missingOptional = missingOptional,
            warnings = warnings,
            structure = structure,
        )
    }

    private fun parseStructure(pkgRoot: File, setFile: File): PackageStructure {
        val props = mutableMapOf<String, String>()
        setFile.readLines().forEach { line ->
            val t = line.trim()
            if (t.isNotBlank() && !t.startsWith("#")) {
                val eq = t.indexOf('=')
                if (eq > 0) props[t.substring(0, eq).trim()] = t.substring(eq + 1).trim()
            }
        }
        val imgDir = File(pkgRoot, "media/image")
        val vidDir = File(pkgRoot, "media/video")
        val fontDir = File(pkgRoot, "media/fonts")

        return PackageStructure(
            hasSetTxt = true,
            hasTemplate = File(pkgRoot, "setting/dlotemplate.xml").exists(),
            hasCryptoProfile = File(pkgRoot, "setting/pro/Best64.xml").exists(),
            hasDocumentation = File(pkgRoot, "setting/Documentation.dlof").exists(),
            hasLicense = File(pkgRoot, "setting/license.dlof").exists(),
            media = MediaSummary(
                imageCount = imgDir.listFiles()?.size ?: 0,
                videoCount = vidDir.listFiles()?.size ?: 0,
                fontCount = fontDir.listFiles { f -> f.extension in listOf("ttf", "otf", "woff", "woff2") }?.size ?: 0,
                hasChapters = imgDir.listFiles { f -> f.isDirectory && f.name.startsWith("chapter") }?.isNotEmpty() ?: false,
                hasEpisodes = File(vidDir, "Episodes").exists(),
            ),
            cryptoEnabled = props["crypto.enabled"]?.toBoolean() ?: false,
        )
    }

    /** يُعيد كل ملفات .dlof داخل الحزمة (بحث متكرر). */
    fun findAllDlofFiles(pkgRoot: File): List<File> =
        pkgRoot.walkTopDown().filter { it.isFile && it.extension == "dlof" }.toList()

    fun calculateSize(pkgRoot: File): Long =
        pkgRoot.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    fun formatSize(bytes: Long): String = when {
        bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.2f MB".format(bytes / 1_048_576.0)
        bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
        else -> "$bytes بايت"
    }

    /** ملخص نصي جاهز للعرض في واجهة المستخدم. */
    fun summaryText(pkgRoot: File): String {
        val check = checkPackage(pkgRoot)
        val s = check.structure ?: return "تعذّر تحليل الحزمة: set.txt مفقود"
        val size = calculateSize(pkgRoot)
        return buildString {
            appendLine("حالة الحزمة: ${if (check.isValid) "✓ صالحة" else "✗ غير مكتملة"}")
            appendLine("القالب: ${if (s.hasTemplate) "موجود" else "غير موجود"}")
            appendLine("التوثيق: ${if (s.hasDocumentation) "موجود" else "غير موجود"}")
            appendLine("الترخيص: ${if (s.hasLicense) "موجود" else "غير موجود"}")
            appendLine("صور: ${s.media.imageCount}${if (s.media.hasChapters) " (مع فصول)" else ""}")
            appendLine("فيديو: ${s.media.videoCount}${if (s.media.hasEpisodes) " (مع حلقات)" else ""}")
            appendLine("خطوط: ${s.media.fontCount}")
            appendLine("التشفير: ${if (s.cryptoEnabled) "مفعّل" else "غير مفعّل"}")
            appendLine("الحجم: ${formatSize(size)}")
            if (check.warnings.isNotEmpty()) {
                appendLine("تحذيرات:")
                check.warnings.forEach { appendLine("  ⚠ $it") }
            }
        }
    }
}
