package com.creativeali.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.creativeali.app.R

/**
 * ══════════════════════════════════════════════════════════════════
 *  DLoF Fonts — 20+ خطاً من Google Fonts + دعم استيراد خط مخصص
 * ══════════════════════════════════════════════════════════════════
 */

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

// ── 1. Noto Naskh Arabic — الافتراضي للعربية ────────────────────
private val notoNaskhArabic = GoogleFont("Noto Naskh Arabic")
val NotoNaskhArabicFamily = FontFamily(
    Font(googleFont = notoNaskhArabic, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = notoNaskhArabic, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = notoNaskhArabic, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = notoNaskhArabic, fontProvider = provider, weight = FontWeight.Bold)
)

// ── 2. Lora — العناوين الإنجليزية ───────────────────────────────
private val loraFont = GoogleFont("Lora")
val LoraFamily = FontFamily(
    Font(googleFont = loraFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = loraFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = loraFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = loraFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = loraFont, fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = loraFont, fontProvider = provider, weight = FontWeight.Bold,   style = FontStyle.Italic)
)

// ── 3. Cairo ─────────────────────────────────────────────────────
val CairoFamily = FontFamily(
    Font(googleFont = GoogleFont("Cairo"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Cairo"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Cairo"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Cairo"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 4. Tajawal ───────────────────────────────────────────────────
val TajawalFamily = FontFamily(
    Font(googleFont = GoogleFont("Tajawal"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Tajawal"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Tajawal"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 5. Almarai ───────────────────────────────────────────────────
val AlmaraiFamily = FontFamily(
    Font(googleFont = GoogleFont("Almarai"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Almarai"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 6. Amiri ─────────────────────────────────────────────────────
val AmiriFamily = FontFamily(
    Font(googleFont = GoogleFont("Amiri"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Amiri"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Amiri"), fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// ── 7. Scheherazade New ──────────────────────────────────────────
val ScheherazadeFamily = FontFamily(
    Font(googleFont = GoogleFont("Scheherazade New"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Scheherazade New"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 8. Vazirmatn ─────────────────────────────────────────────────
val VazirmatnFamily = FontFamily(
    Font(googleFont = GoogleFont("Vazirmatn"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Vazirmatn"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Vazirmatn"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 9. IBM Plex Sans Arabic ──────────────────────────────────────
val IBMPlexArabicFamily = FontFamily(
    Font(googleFont = GoogleFont("IBM Plex Sans Arabic"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("IBM Plex Sans Arabic"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("IBM Plex Sans Arabic"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("IBM Plex Sans Arabic"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 10. Aref Ruqaa ───────────────────────────────────────────────
val ArefRuqaaFamily = FontFamily(
    Font(googleFont = GoogleFont("Aref Ruqaa"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Aref Ruqaa"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 11. Merriweather ─────────────────────────────────────────────
val MerriweatherFamily = FontFamily(
    Font(googleFont = GoogleFont("Merriweather"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Merriweather"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Merriweather"), fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// ── 12. Playfair Display ─────────────────────────────────────────
val PlayfairFamily = FontFamily(
    Font(googleFont = GoogleFont("Playfair Display"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Playfair Display"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Playfair Display"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Playfair Display"), fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// ── 13. Source Sans 3 ────────────────────────────────────────────
val SourceSansFamily = FontFamily(
    Font(googleFont = GoogleFont("Source Sans 3"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Source Sans 3"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Source Sans 3"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 14. Inter ────────────────────────────────────────────────────
val InterFamily = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 15. Roboto ───────────────────────────────────────────────────
val RobotoFamily = FontFamily(
    Font(googleFont = GoogleFont("Roboto"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Roboto"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Roboto"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 16. Nunito ───────────────────────────────────────────────────
val NunitoFamily = FontFamily(
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 17. Poppins ──────────────────────────────────────────────────
val PoppinsFamily = FontFamily(
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Bold)
)

// ── 18. DM Serif Display ─────────────────────────────────────────
val DMSerifFamily = FontFamily(
    Font(googleFont = GoogleFont("DM Serif Display"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("DM Serif Display"), fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// ── 19. Libre Baskerville ────────────────────────────────────────
val LibreBaskervilleFamily = FontFamily(
    Font(googleFont = GoogleFont("Libre Baskerville"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Libre Baskerville"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Libre Baskerville"), fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic)
)

// ── 20. Crimson Pro ──────────────────────────────────────────────
val CrimsonProFamily = FontFamily(
    Font(googleFont = GoogleFont("Crimson Pro"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Crimson Pro"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Crimson Pro"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Crimson Pro"), fontProvider = provider, weight = FontWeight.Bold)
)

// ═══════════════════════════════════════════════════════════════════
//  نظام اختيار الخط — يمكن توسيعه بخط مخصص مستورد
// ═══════════════════════════════════════════════════════════════════

/**
 * كل خط متاح في التطبيق — يُعرض في شاشة الأدوات > اختيار الخط
 */
data class DlofFontOption(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val fontFamily: FontFamily,
    val isCustom: Boolean = false  // خط مستورد من المستخدم
)

val BuiltInFonts: List<DlofFontOption> = listOf(
    DlofFontOption("noto_naskh",    "نوتو نسخ عربي",       "Noto Naskh Arabic",      NotoNaskhArabicFamily),
    DlofFontOption("cairo",         "كايرو",                "Cairo",                  CairoFamily),
    DlofFontOption("tajawal",       "تجوال",                "Tajawal",                TajawalFamily),
    DlofFontOption("almarai",       "المرعي",               "Almarai",                AlmaraiFamily),
    DlofFontOption("amiri",         "أميري",                "Amiri",                  AmiriFamily),
    DlofFontOption("scheherazade",  "شهرزاد",               "Scheherazade New",       ScheherazadeFamily),
    DlofFontOption("vazirmatn",     "وزير متن",             "Vazirmatn",              VazirmatnFamily),
    DlofFontOption("ibm_plex",      "IBM بلكس عربي",        "IBM Plex Sans Arabic",   IBMPlexArabicFamily),
    DlofFontOption("aref_ruqaa",    "عارف رقعة",            "Aref Ruqaa",             ArefRuqaaFamily),
    DlofFontOption("lora",          "لورا",                 "Lora",                   LoraFamily),
    DlofFontOption("merriweather",  "ميريويذر",             "Merriweather",           MerriweatherFamily),
    DlofFontOption("playfair",      "بلايفير",              "Playfair Display",       PlayfairFamily),
    DlofFontOption("source_sans",   "سورس سانس",            "Source Sans 3",          SourceSansFamily),
    DlofFontOption("inter",         "إنتر",                 "Inter",                  InterFamily),
    DlofFontOption("roboto",        "روبوتو",               "Roboto",                 RobotoFamily),
    DlofFontOption("nunito",        "نونيتو",               "Nunito",                 NunitoFamily),
    DlofFontOption("poppins",       "بوبينز",               "Poppins",                PoppinsFamily),
    DlofFontOption("dm_serif",      "دي إم سيريف",          "DM Serif Display",       DMSerifFamily),
    DlofFontOption("libre_bask",    "ليبري باسكرفيل",       "Libre Baskerville",      LibreBaskervilleFamily),
    DlofFontOption("crimson_pro",   "كريمسون برو",          "Crimson Pro",            CrimsonProFamily)
)

/** CompositionLocal للخط المختار حالياً */
val LocalAppFontFamily = compositionLocalOf<FontFamily> { NotoNaskhArabicFamily }
