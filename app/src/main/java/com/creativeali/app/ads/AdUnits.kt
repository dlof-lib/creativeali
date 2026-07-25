package com.creativeali.app.ads

/**
 * معرّفات وحدات الإعلان. معرّفات الاختبار الرسمية من Google مضبوطة افتراضيًا
 * حتى لا يُنشر التطبيق بالخطأ بمعرّفات اختبار حقيقية أو فارغة.
 * استبدل القيم في [Real] بمعرّفاتك الفعلية من AdMob قبل النشر على المتجر،
 * واستخدم [AdUnits.current] للتبديل تلقائيًا حسب [BuildConfigLike.isDebug].
 */
object AdUnits {

    object Test {
        const val BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
        const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    }

    /** ★ عدّل هذه القيم بمعرّفات AdMob الفعلية الخاصة بتطبيقك قبل النشر. */
    object Real {
        const val BANNER = "ca-app-pub-1525040025806904/4931989941"
        const val INTERSTITIAL = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
        const val REWARDED = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
        const val NATIVE = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
        const val APP_OPEN = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
    }

    fun banner(isDebug: Boolean) = if (isDebug) Test.BANNER else Real.BANNER
    fun interstitial(isDebug: Boolean) = if (isDebug) Test.INTERSTITIAL else Real.INTERSTITIAL
    fun rewarded(isDebug: Boolean) = if (isDebug) Test.REWARDED else Real.REWARDED
    fun native(isDebug: Boolean) = if (isDebug) Test.NATIVE else Real.NATIVE
    fun appOpen(isDebug: Boolean) = if (isDebug) Test.APP_OPEN else Real.APP_OPEN
}

/** حدود تكرار عرض الإعلانات — تمنع إزعاج المستخدم بإعلانات متتالية. */
data class AdFrequencyPolicy(
    val minSecondsBetweenInterstitials: Int = 90,
    val maxInterstitialsPerSession: Int = 6,
    val showBannerOnScreens: Set<String> = setOf("library", "reader"),
)
