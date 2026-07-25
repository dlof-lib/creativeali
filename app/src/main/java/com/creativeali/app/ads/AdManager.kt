package com.creativeali.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * إدارة إعلانات Creative Ali عبر Google Mobile Ads SDK. مبني ليكون آمنًا
 * افتراضيًا: يستخدم معرّفات الاختبار الرسمية في وضع Debug، ويحترم
 * [AdFrequencyPolicy] لمنع الإزعاج بإعلانات متكررة.
 *
 * ملاحظة: يتطلب هذا الوحدة اتصالاً بالإنترنت لتحميل وعرض الإعلانات (موجود
 * أصلاً ضمن أذونات التطبيق) وإضافة تبعية `play-services-ads` في build.gradle.kts
 * (أُضيفت بالفعل). لا حاجة لأي إعداد آخر عدا استبدال معرّفات [AdUnits.Real]
 * الحقيقية قبل النشر على المتجر.
 */
object AdManager {

    private const val TAG = "AdManager"
    private var initialized = false
    private var interstitial: InterstitialAd? = null
    private var lastInterstitialShownAt = 0L
    private var interstitialShownCount = 0
    private val policy = AdFrequencyPolicy()

    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        MobileAds.initialize(context.applicationContext) { status ->
            Log.i(TAG, "AdMob initialized: ${status.adapterStatusMap.keys}")
        }
    }

    /** يحمّل إعلانًا بينيًا (interstitial) مسبقًا ليكون جاهزًا عند الحاجة. */
    fun preloadInterstitial(context: Context, isDebug: Boolean) {
        InterstitialAd.load(
            context,
            AdUnits.interstitial(isDebug),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "فشل تحميل الإعلان البيني: ${error.message}")
                    interstitial = null
                }
            }
        )
    }

    /**
     * يعرض الإعلان البيني المحمّل مسبقًا إن سمحت سياسة التكرار [AdFrequencyPolicy]،
     * ثم يعيد تحميل إعلان جديد تلقائيًا لتجهيزه للمرة القادمة.
     */
    fun maybeShowInterstitial(activity: Activity, isDebug: Boolean, onDismissed: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        val ad = interstitial
        val cooldownOk = (now - lastInterstitialShownAt) / 1000 >= policy.minSecondsBetweenInterstitials
        val underLimit = interstitialShownCount < policy.maxInterstitialsPerSession

        if (ad == null || !cooldownOk || !underLimit) {
            onDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                preloadInterstitial(activity, isDebug)
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                onDismissed()
            }
        }
        ad.show(activity)
        lastInterstitialShownAt = now
        interstitialShownCount++
    }
}

/** بانر إعلاني قابل للتضمين مباشرة في أي شاشة Compose.
 *  يعرض إعلانات الاختبار تلقائيًا في بنية Debug فقط، وإعلانات حقيقية
 *  (AdUnits.Real) في بنية Release — دون أي تغيير مطلوب من المتصل. */
@Composable
fun AdBanner(modifier: Modifier = Modifier.fillMaxWidth(), isDebug: Boolean = com.creativeali.app.BuildConfig.DEBUG) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdUnits.banner(isDebug)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
