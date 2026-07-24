package com.creativeali.app

import android.app.Application

/**
 * Creative Ali — entry point Application class.
 * Two feature areas live under this app:
 *  - blogging/   : DLoF-based journal/memoir writing (.dlof, .b.dlof, .dlofpkg)
 *  - diagrams/   : drag & drop canvas for project/app diagrams
 */
class CreativeAliApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
