package com.bel9ja.boardclub.launch

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/** Result of the very first response of the site (redirects are NOT followed). */
internal sealed class ProbeResult {
    /** 200 OK — open the app functionality. */
    object Pass : ProbeResult()

    /** 301/302/303/307/308 with a valid Location on the agreed HTTPS domain — open WebView. */
    data class Redirect(val location: String, val setCookies: List<String>) : ProbeResult()

    /** No answer, invalid answer, or technical error — open the app functionality. */
    data class Failure(val reason: String) : ProbeResult()
}

internal object EntryProbe {

    private val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
    private const val TIMEOUT_MS = 8_000

    /**
     * Blocking GET. Call from a background dispatcher.
     * @param requestUrl entry URL with the attempt id already attached.
     * @param entryUrl entry URL without attempt id (defines the agreed domain).
     * @param headers all device HTTP headers collected by [DeviceHeaders].
     */
    fun probe(
        requestUrl: String,
        entryUrl: String,
        headers: Map<String, String>
    ): ProbeResult {
        if (UrlRules.parseHttps(requestUrl) == null) return ProbeResult.Failure("bad url")
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(requestUrl).openConnection() as HttpsURLConnection).apply {
                instanceFollowRedirects = false
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                useCaches = false
                // Apply all collected device headers
                for ((key, value) in headers) {
                    setRequestProperty(key, value)
                }
            }
            val code = conn.responseCode // TLS errors throw here and are NOT ignored
            when {
                code == 200 -> ProbeResult.Pass
                code in REDIRECT_CODES -> {
                    val rawLocation = conn.getHeaderField("Location")
                    if (rawLocation.isNullOrBlank()) {
                        ProbeResult.Failure("redirect without Location")
                    } else {
                        val absolute = try {
                            URI(requestUrl).resolve(rawLocation.trim()).toString()
                        } catch (e: Exception) {
                            null
                        }
                        if (absolute != null && UrlRules.isTrusted(absolute, entryUrl)) {
                            val cookies = conn.headerFields
                                .filterKeys { it != null && it.equals("Set-Cookie", ignoreCase = true) }
                                .values.flatten()
                            ProbeResult.Redirect(absolute, cookies)
                        } else {
                            ProbeResult.Failure("untrusted Location")
                        }
                    }
                }
                else -> ProbeResult.Failure("http $code") // incl. 304 — not a redirect
            }
        } catch (e: Exception) {
            ProbeResult.Failure(e.javaClass.simpleName)
        } finally {
            try {
                conn?.inputStream?.close()
            } catch (ignored: Exception) {
            }
            conn?.disconnect()
        }
    }
}
