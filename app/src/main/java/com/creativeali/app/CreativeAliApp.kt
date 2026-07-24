package com.creativeali.app

import android.app.Application
import com.creativeali.app.ads.AdManager

/**
 * Creative Ali — entry point Application class.
 * Feature areas:
 *  - blogging/ : DLoF-based journal/memoir writing (.dlof, .b.dlof, .dlofpkg)
 *  - diagrams/ : drag & drop canvas for project/app diagrams
 *  - dlof/     : full DLoF v1.0 format engine (parser, crypto, episodes, templates)
 *  - backup/   : full app backup/restore (.caibak)
 *  - ads/      : AdMob banner + interstitial ads
 */
class CreativeAliApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}
