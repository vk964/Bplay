package com.bel9ja.boardclub.launch

/**
 * Names generated specifically for this app (Bel9ja Board Club).
 * Do NOT rename after release: the Remote Config keys must match the
 * Firebase console, and renaming the local keys would reset stored state.
 *
 * Mapping table: see ENTRY_ROUTING.md in the project root.
 */
internal object RouteKeys {

    // ---- Firebase Remote Config (project "bel9as-club") ----
    /** Boolean: is the site check enabled. In-app default = false. */
    const val RC_CHECK_ENABLED = "kestrel_saffron_njv"

    /** String: HTTPS entry URL of the site. */
    const val RC_ENTRY_URL = "ibis_sorrel_ecx"

    // ---- Short-lived attempt id (query parameter shared with the site) ----
    const val ATTEMPT_PARAM = "welcv"

    // ---- Local storage (SharedPreferences) ----
    const val PREFS_FILE = "basalt_garnet_tk1"
    const val P_HAS_OPENED_WEBVIEW = "marlin_pewter_hrn"   // hasOpenedWebView
    const val P_INITIAL_ENTRY_URL = "umber_garnet_pyi"     // initialEntryUrl
    const val P_LAST_WEBVIEW_URL = "nimbus_quill_trx"      // lastWebViewUrl
    const val P_LAST_URL_BROKEN = "umber_nimbus_as7"       // is lastWebViewUrl broken

    // ---- Deep link back into the app ----
    /** Custom scheme: amberlarch4xo://zephyr-larch-s2p?t=<token> */
    const val DEEPLINK_SCHEME = "amberlarch4xo"
    const val DEEPLINK_HOST = "zephyr-larch-s2p"

    /**
     * Android App Link path (recommended by the spec): https://<APP_LINK_HOST>/zephyr-larch-s2p
     * Matching intent-filter is in AndroidManifest.xml;
     * https://liktoria.org/.well-known/assetlinks.json must be published (see assetlinks.json).
     */
    const val APP_LINK_PATH = "/zephyr-larch-s2p"
    const val APP_LINK_HOST = "liktoria.org"
}
