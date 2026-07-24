package com.creativeali.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography المحسَّن:
 * - العناوين الكبيرة → Lora (serif راقٍ — مناسب للهوية الورقية)
 * - نص الجسم والتسميات → Noto Naskh Arabic (ممتاز للعربية والإنجليزية معاً)
 * - FontFamily.Default كـ fallback للأحرف غير المدعومة
 */
val DlofTypography = Typography(

    // ── عناوين كبيرة — Lora ──────────────────────────────────────
    displaySmall = TextStyle(
        fontFamily = LoraFamily,
        fontWeight  = FontWeight.Bold,
        fontSize    = 32.sp,
        lineHeight  = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = LoraFamily,
        fontWeight  = FontWeight.Bold,
        fontSize    = 28.sp,
        lineHeight  = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = LoraFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 22.sp,
        lineHeight  = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = LoraFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 18.sp,
        lineHeight  = 26.sp
    ),

    // ── عناوين ثانوية — Noto Naskh Arabic ────────────────────────
    titleLarge = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 20.sp,
        lineHeight  = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 17.sp,
        lineHeight  = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp
    ),

    // ── نص الجسم — Noto Naskh Arabic ─────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 17.sp,
        lineHeight  = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 15.sp,
        lineHeight  = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 13.sp,
        lineHeight  = 20.sp
    ),

    // ── تسميات وأزرار — Noto Naskh Arabic ───────────────────────
    labelLarge = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoNaskhArabicFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 11.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp
    )
)
