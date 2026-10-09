package com.bel9ja.boardclub.launch

import android.content.Context
import android.content.SharedPreferences

/** Local per-user state from section 2.2 of the spec. */
internal class RouteStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(RouteKeys.PREFS_FILE, Context.MODE_PRIVATE)

    val hasOpenedWebView: Boolean
        get() = prefs.getBoolean(RouteKeys.P_HAS_OPENED_WEBVIEW, false)

    /** Initial site URL that first returned a redirect (without the attempt id). */
    val initialEntryUrl: String?
        get() = prefs.getString(RouteKeys.P_INITIAL_ENTRY_URL, null)

    /** Last successfully opened main-frame page of the agreed HTTPS site. */
    val lastWebViewUrl: String?
        get() = prefs.getString(RouteKeys.P_LAST_WEBVIEW_URL, null)

    val isLastUrlBroken: Boolean
        get() = prefs.getBoolean(RouteKeys.P_LAST_URL_BROKEN, false)

    /** First redirect received: remember the entry URL and the "WebView shown" flag. */
    fun markFirstWebViewEntry(initialEntryUrl: String) {
        prefs.edit()
            .putBoolean(RouteKeys.P_HAS_OPENED_WEBVIEW, true)
            .putString(RouteKeys.P_INITIAL_ENTRY_URL, initialEntryUrl)
            .apply()
    }

    fun saveLastWebViewUrl(url: String) {
        prefs.edit()
            .putString(RouteKeys.P_LAST_WEBVIEW_URL, url)
            .putBoolean(RouteKeys.P_LAST_URL_BROKEN, false)
            .apply()
    }

    fun markLastUrlBroken() {
        prefs.edit().putBoolean(RouteKeys.P_LAST_URL_BROKEN, true).apply()
    }
}
