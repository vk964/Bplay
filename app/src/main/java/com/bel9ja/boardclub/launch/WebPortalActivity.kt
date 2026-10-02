package com.bel9ja.boardclub.launch

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bel9ja.boardclub.MainActivity
import kotlinx.coroutines.launch

/**
 * WebView screen. Saves only successfully opened main-frame pages of the
 * agreed HTTPS site as lastWebViewUrl; deep links back to the app are
 * handled here and never stored.
 */
class WebPortalActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_RESUME = "extra_resume"

        internal fun intent(context: Context, url: String, resume: Boolean): Intent =
            Intent(context, WebPortalActivity::class.java)
                .putExtra(EXTRA_URL, url)
                .putExtra(EXTRA_RESUME, resume)
    }

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var store: RouteStore
    private lateinit var router: LaunchRouter

    /** Current load is a reopen of lastWebViewUrl (section 2.2). */
    private var resumeMode = false

    /** At least one main page loaded fine during this screen session. */
    private var hadSuccessfulPage = false

    /** Error flags for the current main-frame navigation. */
    private var mainFrameFailed = false
    private var mainFrameHttpError = false
    private var currentMainUrl: String? = null

    private var routingAway = false
    private val sslFailedUrls = HashSet<String>()

    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            fileCallback?.onReceiveValue(uris)
            fileCallback = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = RouteStore(this)
        router = LaunchRouter(this)

        val url = intent.getStringExtra(EXTRA_URL)
        resumeMode = intent.getBooleanExtra(EXTRA_RESUME, false)
        if (url.isNullOrBlank()) {
            openApp(null)
            return
        }

        buildUi()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        val restored = savedInstanceState?.let { webView.restoreState(it) }
        if (restored == null) webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildUi() {
        val root = FrameLayout(this).apply { setBackgroundColor(Color.parseColor("#0F1830")) }
        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#0F1830"))
        }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 8)
            max = 100
            isIndeterminate = false
        }
        root.addView(webView)
        root.addView(progress)
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
            allowFileAccess = false
            allowContentAccess = false
            mediaPlaybackRequiresUserGesture = false
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = PortalClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }

            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return try {
                    fileChooser.launch(params?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).setType("*/*"))
                    true
                } catch (e: ActivityNotFoundException) {
                    fileCallback = null
                    false
                }
            }
        }
    }

    private inner class PortalClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri = request.url
            if (UrlRules.isAppDeepLink(uri)) {
                // Return to the app — handled first, never saved as lastWebViewUrl.
                openApp(uri)
                return true
            }
            return when (uri.scheme?.lowercase()) {
                "https", "http" -> false // http is blocked by cleartext policy -> main-frame error
                "intent" -> {
                    handleIntentScheme(uri.toString())
                    true
                }
                else -> {
                    openExternal(uri)
                    true
                }
            }
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            mainFrameFailed = false
            mainFrameHttpError = false
            currentMainUrl = url
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            // Sub-resource errors are ignored; only the main page counts.
            if (request.isForMainFrame) {
                mainFrameFailed = true
                onMainFrameFailure()
            }
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            if (!request.isForMainFrame) return
            val code = errorResponse.statusCode
            if (code >= 400) mainFrameHttpError = true // never saved as lastWebViewUrl
            // A vanished page (404/410) means a stale address.
            if (code == 404 || code == 410) {
                mainFrameFailed = true
                onMainFrameFailure()
            }
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.cancel() // TLS errors are never ignored
            error.url?.let { sslFailedUrls.add(it) }
            val isMain = currentMainUrl == null || error.url == currentMainUrl
            if (isMain) {
                mainFrameFailed = true
                onMainFrameFailure()
            }
        }

        override fun onPageFinished(view: WebView, url: String?) {
            if (url != null && url in sslFailedUrls) {
                mainFrameFailed = true
                onMainFrameFailure()
            }
            if (mainFrameFailed || mainFrameHttpError || url == null) return
            if (UrlRules.isAppDeepLink(Uri.parse(url))) return
            if (!UrlRules.isTrusted(url, store.initialEntryUrl ?: url)) return
            hadSuccessfulPage = true
            store.saveLastWebViewUrl(UrlRules.stripAttempt(url))
        }
    }

    /** Main page failed (network, TLS, stale address). */
    private fun onMainFrameFailure() {
        if (routingAway) return
        if (hadSuccessfulPage) {
            // Site already worked in this session: keep the WebView's error page,
            // user may retry; don't kick them out on a network blip.
            return
        }
        routingAway = true
        store.markLastUrlBroken()
        if (!resumeMode) {
            openApp(null) // technical pass
            return
        }
        // lastWebViewUrl stopped working -> once per launch, re-check initialEntryUrl.
        lifecycleScope.launch {
            when (val d = router.reprobeInitialEntry()) {
                is Destination.Web -> {
                    routingAway = false
                    resumeMode = false
                    webView.stopLoading()
                    webView.clearHistory()
                    webView.loadUrl(d.url)
                }
                Destination.App -> openApp(null)
            }
        }
    }

    private fun handleIntentScheme(raw: String) {
        try {
            val intent = Intent.parseUri(raw, Intent.URI_INTENT_SCHEME)
            if (UrlRules.isAppDeepLink(intent.data)) {
                openApp(intent.data)
                return
            }
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.component = null
            intent.selector = null
            startActivity(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun openExternal(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE))
        } catch (e: Exception) {
            // no handler
        }
    }

    /** Opens the native app functionality (deep-link return or technical pass). */
    private fun openApp(deepLink: Uri?) {
        routingAway = true
        val i = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_OPEN_APP, true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        if (deepLink != null) i.data = deepLink
        startActivity(i)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::webView.isInitialized) webView.saveState(outState)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        super.onDestroy()
    }
}
