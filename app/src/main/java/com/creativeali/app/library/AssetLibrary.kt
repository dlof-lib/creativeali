package com.creativeali.app.library

import android.content.Context

/**
 * Lists the bundled fonts (assets/fonts/*.ttf|*.otf) and icons
 * (assets/icons/*.svg|*.png|*.xml) so the diagram editor can offer them as a
 * picker. Drop new font/icon files straight into those asset folders — no
 * code changes needed, they show up automatically.
 */
object FontLibrary {
    fun list(context: Context): List<String> =
        context.assets.list("fonts")
            ?.filter { it.endsWith(".ttf") || it.endsWith(".otf") }
            ?.sorted() ?: emptyList()
}

object IconLibrary {
    fun list(context: Context): List<String> =
        context.assets.list("icons")
            ?.filter { it.endsWith(".svg") || it.endsWith(".png") || it.endsWith(".xml") }
            ?.sorted() ?: emptyList()
}
