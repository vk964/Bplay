package com.bel9ja.boardclub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bel9ja.boardclub.launch.LaunchState
import com.bel9ja.boardclub.launch.LaunchViewModel
import com.bel9ja.boardclub.launch.UrlRules
import com.bel9ja.boardclub.launch.WebPortalActivity
import com.bel9ja.boardclub.ui.AppRoot
import com.bel9ja.boardclub.ui.theme.Bel9jaBoardClubTheme
import com.bel9ja.boardclub.ui.theme.Navy950
import com.bel9ja.boardclub.ui.theme.Orange
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        /** Set when returning from the WebView (deep link or technical pass). */
        const val EXTRA_OPEN_APP = "extra_open_app"
    }

    private val launchVm: LaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        when {
            // Deep-link return is NOT a normal launch: open the functionality first,
            // so there is no "WebView -> app -> WebView" loop.
            isAppReturn(intent) -> launchVm.openAppDirectly()
            // Recreated after process death while the user was inside the app.
            savedInstanceState != null && launchVm.isIdle -> launchVm.openAppDirectly()
            else -> launchVm.startIfNeeded()
        }

        setContent {
            Bel9jaBoardClubTheme {
                val state by launchVm.state.collectAsState()
                when (state) {
                    LaunchState.App -> AppRoot()
                    else -> LaunchSplash()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchVm.state.collect { s ->
                    if (s is LaunchState.Web && !launchVm.webHandedOff) {
                        launchVm.webHandedOff = true
                        startActivity(WebPortalActivity.intent(this@MainActivity, s.url, s.resume))
                        finish()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isAppReturn(intent)) launchVm.openAppDirectly()
    }

    private fun isAppReturn(intent: Intent?): Boolean {
        if (intent == null) return false
        if (intent.getBooleanExtra(EXTRA_OPEN_APP, false)) return true
        return intent.action == Intent.ACTION_VIEW && UrlRules.isAppDeepLink(intent.data)
    }
}

@Composable
private fun LaunchSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Orange)
    }
}
