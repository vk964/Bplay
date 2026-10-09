package com.bel9ja.boardclub.launch

import android.content.Context
import android.os.Build
import android.webkit.WebSettings
import java.util.Locale
import java.util.TimeZone

/**
 * Collects all relevant HTTP headers that identify the user's device.
 * Both EntryProbe and WebPortalActivity use the same set so the
 * verification service sees identical fingerprints on the probe GET
 * and the subsequent WebView load.
 */
internal object DeviceHeaders {

    /**
     * Builds the full header map.
     *
     * @param context application context (needed for WebView User-Agent)
     * @param cookieHeader current cookie string for the target URL (may be null)
     */
    fun collect(context: Context, cookieHeader: String? = null): Map<String, String> {
        val headers = LinkedHashMap<String, String>()

        // ---- User-Agent (same as WebView) ----
        val ua = try {
            WebSettings.getDefaultUserAgent(context)
        } catch (_: Exception) {
            "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }
        headers["User-Agent"] = ua

        // ---- Accept ----
        headers["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"

        // ---- Accept-Language ----
        headers["Accept-Language"] = buildAcceptLanguage()

        // ---- Accept-Encoding ----
        headers["Accept-Encoding"] = "gzip, deflate, br"

        // ---- X-Requested-With (identifies the app package) ----
        headers["X-Requested-With"] = context.packageName

        // ---- Device / OS identification ----
        headers["X-Device-Model"] = Build.MODEL
        headers["X-Device-Brand"] = Build.BRAND
        headers["X-Device-Manufacturer"] = Build.MANUFACTURER
        headers["X-OS-Version"] = Build.VERSION.RELEASE
        headers["X-SDK-Version"] = Build.VERSION.SDK_INT.toString()
        headers["X-Device-Product"] = Build.PRODUCT
        headers["X-Device-Hardware"] = Build.HARDWARE
        headers["X-Device-Fingerprint"] = Build.FINGERPRINT

        // ---- Timezone ----
        headers["X-Timezone"] = TimeZone.getDefault().id

        // ---- Display language ----
        headers["X-Device-Language"] = Locale.getDefault().toLanguageTag()

        // ---- Cache control (same as probe) ----
        headers["Cache-Control"] = "no-cache"
        headers["Pragma"] = "no-cache"

        // ---- Cookies ----
        if (!cookieHeader.isNullOrBlank()) {
            headers["Cookie"] = cookieHeader
        }

        return headers
    }

    /** Builds an Accept-Language value from the device's preferred locales. */
    private fun buildAcceptLanguage(): String {
        val locales = try {
            val localeList = androidx.core.os.LocaleListCompat.getDefault()
            (0 until localeList.size()).mapNotNull { localeList[it] }
        } catch (_: Exception) {
            listOf(Locale.getDefault())
        }
        if (locales.isEmpty()) return Locale.getDefault().toLanguageTag()

        return locales.mapIndexed { index, locale ->
            val tag = locale.toLanguageTag()
            if (index == 0) tag else {
                val q = (10 - index).coerceAtLeast(1).let { "0.${it}" }
                "$tag;q=$q"
            }
        }.joinToString(", ")
    }
}
