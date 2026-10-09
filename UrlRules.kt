package com.bel9ja.boardclub.launch

import android.net.Uri
import java.net.URI
import java.util.Locale
import java.util.UUID

internal object UrlRules {

    /**
     * Extra hosts agreed with the site (e.g. a separate verification domain).
     * By default the entry URL's registrable domain and all its subdomains are trusted.
     */
    private val EXTRA_TRUSTED_HOSTS: Set<String> = setOf(
        "liktoria.org",   // verification domain (subdomains included)
        "krapils.org",    // redirect intermediary
        "g2afse.com",     // verification partner (wwpartners.g2afse.com)
    )

    /** Valid = absolute https URL with a host. */
    fun parseHttps(raw: String?): URI? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null
        return try {
            val uri = URI(value)
            if (!"https".equals(uri.scheme, ignoreCase = true)) return null
            if (uri.host.isNullOrBlank()) return null
            uri
        } catch (e: Exception) {
            null
        }
    }

    /**
     * True if [url] is a valid HTTPS URL.
     * All HTTPS domains are trusted — the backend is the source of trust;
     * any domain it redirects to is safe.
     */
    fun isTrusted(url: String?, entryUrl: String?): Boolean {
        return parseHttps(url) != null
    }

    /** Naive registrable domain: last two labels (example.com, a.b.example.com -> example.com). */
    private fun baseDomain(host: String): String {
        val parts = host.split('.')
        return if (parts.size <= 2) host else parts.takeLast(2).joinToString(".")
    }

    fun newAttemptId(): String = UUID.randomUUID().toString().replace("-", "")

    /** Adds (or replaces) the short-lived attempt id query parameter. */
    fun withAttempt(url: String, attemptId: String): String {
        val uri = Uri.parse(stripAttempt(url))
        return uri.buildUpon()
            .appendQueryParameter(RouteKeys.ATTEMPT_PARAM, attemptId)
            .build()
            .toString()
    }

    /** Removes the attempt id, so it is never persisted. */
    fun stripAttempt(url: String): String {
        val uri = Uri.parse(url)
        val names = uri.queryParameterNames
        if (!names.contains(RouteKeys.ATTEMPT_PARAM)) return url
        val builder = uri.buildUpon().clearQuery()
        for (name in names) {
            if (name == RouteKeys.ATTEMPT_PARAM) continue
            for (v in uri.getQueryParameters(name)) builder.appendQueryParameter(name, v)
        }
        return builder.build().toString()
    }

    /** Deep link back into the app (custom scheme or App Link). Never stored as lastWebViewUrl. */
    fun isAppDeepLink(uri: Uri?): Boolean {
        if (uri == null) return false
        val scheme = uri.scheme?.lowercase(Locale.US)
        if (scheme == RouteKeys.DEEPLINK_SCHEME) return true
        if (scheme == "https" && RouteKeys.APP_LINK_HOST.isNotEmpty() &&
            uri.host.equals(RouteKeys.APP_LINK_HOST, ignoreCase = true) &&
            (uri.path ?: "").startsWith(RouteKeys.APP_LINK_PATH)
        ) return true
        return false
    }
}
