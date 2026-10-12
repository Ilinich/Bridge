package com.begoml.bridge

import coil3.ImageLoader
import coil3.map.Mapper
import coil3.request.Options
import kotlinx.browser.window

private const val CdnPrefix = "https://r2.thesportsdb.com/images/media"
private const val SameOriginPrefix = "/media"

/**
 * Sends the pictures through this origin instead of straight at the CDN.
 *
 * The CDN answers a native client happily and a browser not at all: it sends no
 * `Access-Control-Allow-Origin`, so every image the other three platforms load is blocked here.
 * There is nothing to fix on the client — the rule is the server's — so the web build asks its
 * own origin for `/media/...` and whatever serves the app rewrites that to the CDN. The dev
 * server does it in `webpack.config.d/media-proxy.js`; a deployed build needs the same rule.
 */
internal actual fun ImageLoader.Builder.platformImages(): ImageLoader.Builder =
    components { add(SameOriginMedia()) }

private class SameOriginMedia : Mapper<String, String> {

    /**
     * Absolute, not "/media/...": the HTTP client behind the loader resolves nothing against the
     * page, and hands a relative path back as a failure nobody sees.
     */
    override fun map(data: String, options: Options): String =
        if (data.startsWith(CdnPrefix)) {
            window.location.origin + SameOriginPrefix + data.removePrefix(CdnPrefix)
        } else {
            data
        }
}
