package com.creativeali.app.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

/**
 * هوية بصرية منسوخة بالكامل من تطبيق DLoF: أخضر داكن كلون أساسي،
 * ذهبي/برتقالي كلون ثانوي، وأسود قريب من الحبر كخلفية للوضع الليلي،
 * مع خلفية فاتحة هادئة قريبة من لون خلفية الشعار.
 */

val DlofParchment = Color(0xFFEDEFF7)   // خلفية فاتحة (تطابق خلفية الشعار)
val DlofInk = Color(0xFF0B0B14)         // أسود قريب من حرف الشعار (نص الوضع النهاري)
val DlofCopper = Color(0xFF1D7A3F)      // أخضر داكن — اللون الأساسي (من الشعار)
val DlofMoss = Color(0xFFD2A020)        // ذهبي/برتقالي — اللون الثانوي (من الشعار)
val DlofSurfaceDark = Color(0xFF0A0A14) // أسود الشعار كخلفية للوضع الليلي
val DlofInkLight = Color(0xFFECEEF6)    // نص فاتح للوضع الليلي

private val DlofLightScheme = lightColorScheme(
    primary = DlofCopper,
    onPrimary = Color.White,
    secondary = DlofMoss,
    onSecondary = Color.White,
    background = DlofParchment,
    onBackground = DlofInk,
    surface = Color(0xFFF9FAFD),
    onSurface = DlofInk,
    surfaceVariant = Color(0xFFE3E7F0),
    onSurfaceVariant = DlofInk
)

private val DlofDarkScheme = darkColorScheme(
    primary = Color(0xFF4CC47B),
    onPrimary = DlofSurfaceDark,
    secondary = Color(0xFFE8C24A),
    onSecondary = DlofSurfaceDark,
    background = DlofSurfaceDark,
    onBackground = DlofInkLight,
    surface = Color(0xFF15151F),
    onSurface = DlofInkLight,
    surfaceVariant = Color(0xFF232336),
    onSurfaceVariant = DlofInkLight
)

private const val PREF_FONT = "dlof_prefs"
private const val KEY_FONT_ID = "selected_font_id"

private fun readSelectedFontFamily(context: Context): FontFamily {
    val id = context.getSharedPreferences(PREF_FONT, Context.MODE_PRIVATE)
        .getString(KEY_FONT_ID, "noto_naskh") ?: "noto_naskh"
    return BuiltInFonts.firstOrNull { it.id == id }?.fontFamily ?: NotoNaskhArabicFamily
}

/** يبني Typography ديناميكياً بناءً على الخط المختار من المستخدم،
 *  مع الإبقاء على Lora للعناوين الكبيرة كجزء من الهوية البصرية. */
private fun buildTypography(bodyFont: FontFamily): Typography {
    val base = DlofTypography
    return base.copy(
        bodyLarge = base.bodyLarge.copy(fontFamily = bodyFont),
        bodyMedium = base.bodyMedium.copy(fontFamily = bodyFont),
        bodySmall = base.bodySmall.copy(fontFamily = bodyFont),
        titleLarge = base.titleLarge.copy(fontFamily = bodyFont),
        titleMedium = base.titleMedium.copy(fontFamily = bodyFont),
        titleSmall = base.titleSmall.copy(fontFamily = bodyFont),
        labelLarge = base.labelLarge.copy(fontFamily = bodyFont),
        labelMedium = base.labelMedium.copy(fontFamily = bodyFont),
        labelSmall = base.labelSmall.copy(fontFamily = bodyFont)
    )
}

@Composable
fun CreativeAliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DlofDarkScheme else DlofLightScheme
    val context = LocalContext.current
    val selectedFont = remember { readSelectedFontFamily(context) }
    val typography = remember(selectedFont) { buildTypography(selectedFont) }

    CompositionLocalProvider(LocalAppFontFamily provides selectedFont) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
