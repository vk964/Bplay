package com.bel9ja.boardclub.launch

import android.content.Context
import android.webkit.CookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Where the app should go after the launch decision. */
internal sealed class Destination {
    /** Open the native app functionality. */
    object App : Destination()

    /**
     * Open the WebView.
     * @param url URL to load (may carry the attempt id).
     * @param resume true = reopening the stored lastWebViewUrl (section 2.2),
     *               false = fresh entry URL that just returned a redirect.
     */
    data class Web(val url: String, val resume: Boolean) : Destination()
}

/** Per-launch flags (reset on every normal launch). */
internal object LaunchSession {
    @Volatile
    var fallbackUsed: Boolean = false

    fun reset() {
        fallbackUsed = false
    }
}

/** Implements sections 2.1, 2.2 and 4 of the spec. Call from the main thread. */
internal class LaunchRouter(context: Context) {

    private val appContext = context.applicationContext
    private val store = RouteStore(appContext)

    /** Decision for a normal launch (not a deep-link return). */
    suspend fun decide(): Destination {
        LaunchSession.reset()
        return if (store.hasOpenedWebView) decideReturning() else decideFirstTime()
    }

    // ---- 2.1 / 4: user has never been in the WebView ----
    private suspend fun decideFirstTime(): Destination {
        val config = RemoteSwitch.load(appContext) ?: return Destination.App
        if (!config.enabled) return Destination.App
        val entry = UrlRules.parseHttps(config.entryUrl)?.toString() ?: return Destination.App
        val cleanEntry = UrlRules.stripAttempt(entry)

        val requestUrl = UrlRules.withAttempt(cleanEntry, UrlRules.newAttemptId())
        return when (val r = probe(requestUrl, cleanEntry)) {
            is ProbeResult.Pass -> Destination.App // do not set the WebView flag
            is ProbeResult.Redirect -> {
                store.markFirstWebViewEntry(cleanEntry)
                applyCookies(requestUrl, r.setCookies)
                Destination.Web(requestUrl, resume = false)
            }
            is ProbeResult.Failure -> Destination.App
        }
    }

    // ---- 2.2: user has already been in the WebView (Remote Config flag is NOT used) ----
    private suspend fun decideReturning(): Destination {
        val last = store.lastWebViewUrl
        val entry = store.initialEntryUrl
        if (!last.isNullOrBlank() && !store.isLastUrlBroken && UrlRules.isTrusted(last, entry ?: last)) {
            return Destination.Web(last, resume = true)
        }
        // lastWebViewUrl absent or not working -> mark broken and go back to initialEntryUrl.
        store.markLastUrlBroken()
        return reprobeInitialEntry()
    }

    /**
     * New GET to the stored initialEntryUrl with a new attempt id.
     * At most once per launch; otherwise -> app functionality.
     */
    suspend fun reprobeInitialEntry(): Destination {
        if (LaunchSession.fallbackUsed) return Destination.App
        LaunchSession.fallbackUsed = true

        val entry = UrlRules.parseHttps(store.initialEntryUrl)?.toString() ?: return Destination.App
        val requestUrl = UrlRules.withAttempt(entry, UrlRules.newAttemptId())
        return when (val r = probe(requestUrl, entry)) {
            // 200 -> app; lastWebViewUrl stays broken, so the next launch checks initialEntryUrl again.
            is ProbeResult.Pass -> Destination.App
            is ProbeResult.Redirect -> {
                applyCookies(requestUrl, r.setCookies)
                Destination.Web(requestUrl, resume = false)
            }
            // Initial URL unavailable -> app; retry on the next launch.
            is ProbeResult.Failure -> Destination.App
        }
    }

    private suspend fun probe(requestUrl: String, entryUrl: String): ProbeResult {
        val cookie = try {
            CookieManager.getInstance().getCookie(requestUrl)
        } catch (e: Exception) {
            null
        }
        val headers = DeviceHeaders.collect(appContext, cookie)
        return withContext(Dispatchers.IO) { EntryProbe.probe(requestUrl, entryUrl, headers) }
    }

    /** Share the probe's cookies with the WebView so both requests are tied to one attempt. */
    private fun applyCookies(url: String, cookies: List<String>) {
        if (cookies.isEmpty()) return
        try {
            val cm = CookieManager.getInstance()
            cookies.forEach { cm.setCookie(url, it) }
            cm.flush()
        } catch (e: Exception) {
            // ignore — the attempt id in the URL still ties the two requests
        }
    }
}
