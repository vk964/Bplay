package com.bel9ja.boardclub.launch

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Internal representation of the remote switch (section 4). */
internal data class EntryConfig(val enabled: Boolean, val entryUrl: String?)

internal object RemoteSwitch {

    private const val OVERALL_TIMEOUT_MS = 10_000L

    /**
     * Fetches + activates Remote Config and maps the two generated keys to
     * internal `enabled` / `entryUrl`. Returns null on any error or missing key.
     */
    suspend fun load(context: Context): EntryConfig? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context)
            val rc = FirebaseRemoteConfig.getInstance()
            val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            rc.setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setFetchTimeoutInSeconds(8)
                    .setMinimumFetchIntervalInSeconds(if (debuggable) 0 else 600)
                    .build()
            )
            // Local default: check disabled.
            rc.setDefaultsAsync(mapOf<String, Any>(RouteKeys.RC_CHECK_ENABLED to false))

            val ok = withTimeoutOrNull(OVERALL_TIMEOUT_MS) { fetchAndActivate(rc) } ?: false
            if (!ok) return null

            val enabledValue = rc.getValue(RouteKeys.RC_CHECK_ENABLED)
            val urlValue = rc.getValue(RouteKeys.RC_ENTRY_URL)
            // Key absent on the server -> treat as missing config.
            if (enabledValue.source != FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) return null

            val enabled = try {
                enabledValue.asBoolean()
            } catch (e: IllegalArgumentException) {
                return null
            }
            val url = if (urlValue.source == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                urlValue.asString().trim().takeIf { it.isNotEmpty() }
            } else null
            EntryConfig(enabled, url)
        } catch (e: Exception) {
            null
        }
    }

    /** true = fetch succeeded (activated or already up to date); false = error. */
    private suspend fun fetchAndActivate(rc: FirebaseRemoteConfig): Boolean =
        suspendCancellableCoroutine { cont ->
            rc.fetchAndActivate().addOnCompleteListener { task ->
                if (cont.isActive) cont.resume(task.isSuccessful)
            }
        }
}
