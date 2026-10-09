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
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bel9ja.boardclub.MainActivity
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URI
import android.view.WindowManager
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * WebView screen. Saves only successfully opened main-frame pages of the
 * agreed HTTPS site as lastWebViewUrl; deep links back to the app are
 * handled here and never stored.
 *
 * All user HTTP headers (device model, OS, language, User-Agent, etc.)
 * are forwarded on every request to trusted domains so the verification
 * service receives a complete device fingerprint.
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

    /** All device HTTP headers to forward on every trusted request. */
    private lateinit var deviceHeaders: Map<String, String>

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
    private var cameraPhotoUri: Uri? = null

    /* ── Fullscreen video support ── */
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalSystemUiVisibility: Int = 0

    /* ── Geolocation ── */
    private var geoCallback: GeolocationPermissions.Callback? = null
    private var geoOrigin: String? = null

    private val geoPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results.values.any { it }
            geoCallback?.invoke(geoOrigin, granted, false)
            geoCallback = null
            geoOrigin = null
        }

    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val dataUri = result.data?.data
                if (dataUri != null) {
                    fileCallback?.onReceiveValue(arrayOf(dataUri))
                } else {
                    val photoUri = cameraPhotoUri
                    if (photoUri != null) {
                        fileCallback?.onReceiveValue(arrayOf(photoUri))
                    } else {
                        fileCallback?.onReceiveValue(null)
                    }
                }
            } else {
                fileCallback?.onReceiveValue(null)
            }
            fileCallback = null
            cameraPhotoUri = null
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            // Permission result received
        }

    /** Pending WebRTC / getUserMedia permission request from the page. */
    private var pendingPermissionRequest: PermissionRequest? = null

    private val webPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val req = pendingPermissionRequest ?: return@registerForActivityResult
            val granted = req.resources.filter { res ->
                when (res) {
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                        grants[Manifest.permission.CAMERA] == true
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                        grants[Manifest.permission.RECORD_AUDIO] == true
                    else -> true
                }
            }.toTypedArray()
            if (granted.isNotEmpty()) req.grant(granted) else req.deny()
            pendingPermissionRequest = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        store = RouteStore(this)
        router = LaunchRouter(this)

        val url = intent.getStringExtra(EXTRA_URL)
        resumeMode = intent.getBooleanExtra(EXTRA_RESUME, false)
        if (url.isNullOrBlank()) {
            openApp(null)
            return
        }

        val cookie = try {
            CookieManager.getInstance().getCookie(url)
        } catch (_: Exception) { null }
        deviceHeaders = DeviceHeaders.collect(applicationContext, cookie)

        buildUi()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        val restored = savedInstanceState?.let { webView.restoreState(it) }
        if (restored == null) webView.loadUrl(url, deviceHeaders)
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
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            databaseEnabled = true
            setGeolocationEnabled(true)
            builtInZoomControls = false
            displayZoomControls = false

            // Remove WebView marker so slot providers don't block us
            val defaultUa = userAgentString
            userAgentString = defaultUa.replace("; wv)", ")")
        }

        // Remove X-Requested-With header that identifies this as a WebView app
        if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
            androidx.webkit.WebSettingsCompat.setRequestedWithHeaderOriginAllowList(
                webView.settings, emptySet()
            )
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        // Hardware-accelerated rendering for Canvas / WebGL (slots, games)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
        }

        webView.webViewClient = PortalClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                val newWebView = WebView(this@WebPortalActivity).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.setSupportMultipleWindows(true)
                }
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean {
                        val targetUrl = request.url.toString()
                        val entryHost = try { URI(store.initialEntryUrl ?: "").host } catch (_: Exception) { null }
                        val targetHost = request.url?.host
                        if (entryHost != null && targetHost.equals(entryHost, ignoreCase = true)) {
                            webView.loadUrl(targetUrl, DeviceHeaders.collect(this@WebPortalActivity))
                        } else {
                            webView.loadUrl(targetUrl)
                        }
                        newWebView.destroy()
                        return true
                    }
                }
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

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
                    val intents = mutableListOf<Intent>()

                    if (ContextCompat.checkSelfPermission(this@WebPortalActivity, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        val photoFile = createImageFile()
                        if (photoFile != null) {
                            cameraPhotoUri = FileProvider.getUriForFile(
                                this@WebPortalActivity,
                                "${packageName}.fileprovider",
                                photoFile
                            )
                            intents.add(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
                            })
                        }
                    } else {
                        ActivityCompat.requestPermissions(
                            this@WebPortalActivity,
                            arrayOf(Manifest.permission.CAMERA), 1001
                        )
                    }

                    val pickerIntent = params?.createIntent()
                        ?: Intent(Intent.ACTION_GET_CONTENT).setType("*/*")

                    val chooser = Intent.createChooser(pickerIntent, null)
                    if (intents.isNotEmpty()) {
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
                    }
                    fileChooser.launch(chooser)
                    true
                } catch (e: ActivityNotFoundException) {
                    fileCallback = null
                    false
                }
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                val resources = request.resources
                val androidPerms = mutableListOf<String>()
                for (res in resources) {
                    when (res) {
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                            androidPerms.add(Manifest.permission.CAMERA)
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                            androidPerms.add(Manifest.permission.RECORD_AUDIO)
                    }
                }
                if (androidPerms.isEmpty()) {
                    request.grant(resources)
                    return
                }
                val allGranted = androidPerms.all {
                    ContextCompat.checkSelfPermission(this@WebPortalActivity, it) == PackageManager.PERMISSION_GRANTED
                }
                if (allGranted) {
                    request.grant(resources)
                } else {
                    pendingPermissionRequest = request
                    webPermissionLauncher.launch(androidPerms.toTypedArray())
                }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) { callback?.onCustomViewHidden(); return }
                customView = view
                customViewCallback = callback
                originalSystemUiVisibility = window.decorView.systemUiVisibility
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
                (window.decorView as FrameLayout).addView(
                    customView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
                webView.visibility = View.GONE
            }

            override fun onHideCustomView() {
                if (customView == null) return
                webView.visibility = View.VISIBLE
                (window.decorView as FrameLayout).removeView(customView)
                customView = null
                window.decorView.systemUiVisibility = originalSystemUiVisibility
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                val fineGranted = ContextCompat.checkSelfPermission(
                    this@WebPortalActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (fineGranted) {
                    callback?.invoke(origin, true, false)
                } else {
                    geoCallback = callback
                    geoOrigin = origin
                    geoPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }

    private inner class PortalClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri = request.url
            if (UrlRules.isAppDeepLink(uri)) {
                openApp(uri)
                return true
            }
            return when (uri.scheme?.lowercase()) {
                "https", "http" -> {
                    val urlStr = uri.toString()
                    if (UrlRules.isTrusted(urlStr, store.initialEntryUrl ?: urlStr)) {
                        // Only inject device headers for requests to our own backend host.
                        // External hosts (slot providers, CDNs) navigate natively.
                        val entryHost = try { URI(store.initialEntryUrl ?: "").host } catch (_: Exception) { null }
                        val targetHost = uri.host
                        if (entryHost != null && targetHost.equals(entryHost, ignoreCase = true)) {
                            view.loadUrl(urlStr, deviceHeaders)
                            true
                        } else {
                            false   // let WebView handle external navigation natively
                        }
                    } else {
                        false
                    }
                }
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

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            if (request.isForMainFrame) return null

            val url = request.url?.toString() ?: return null
            if (!url.startsWith("https://", ignoreCase = true)) return null
            if (!UrlRules.isTrusted(url, store.initialEntryUrl ?: url)) return null

            // Only proxy sub-resources going to the SAME host as our entry URL.
            // External CDNs (slot providers, analytics, etc.) pass through natively.
            val entryHost = try { URI(store.initialEntryUrl ?: "").host } catch (_: Exception) { null }
            val requestHost = request.url?.host
            if (entryHost == null || requestHost == null || !requestHost.equals(entryHost, ignoreCase = true)) {
                return null
            }

            val method = request.method ?: "GET"
            if (method.equals("OPTIONS", ignoreCase = true)) return null

            return try {
                val conn = (URL(url).openConnection() as HttpsURLConnection).apply {
                    requestMethod = method
                    instanceFollowRedirects = true
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    useCaches = true

                    for ((key, value) in deviceHeaders) {
                        if (key.equals("Accept-Encoding", ignoreCase = true)) continue
                        setRequestProperty(key, value)
                    }

                    for ((key, value) in request.requestHeaders) {
                        if (key.equals("Accept-Encoding", ignoreCase = true)) continue
                        setRequestProperty(key, value)
                    }

                    val cookie = CookieManager.getInstance().getCookie(url)
                    if (!cookie.isNullOrBlank()) {
                        setRequestProperty("Cookie", cookie)
                    }
                }

                val code = conn.responseCode
                val message = conn.responseMessage ?: "OK"
                val contentType = conn.contentType ?: "text/html"
                val encoding = conn.contentEncoding

                conn.headerFields
                    ?.filterKeys { it != null && it.equals("Set-Cookie", ignoreCase = true) }
                    ?.values?.flatten()?.forEach { setCookie ->
                        CookieManager.getInstance().setCookie(url, setCookie)
                    }

                val parts = contentType.split(";").map { it.trim() }
                val mime = parts.firstOrNull() ?: "text/html"
                val charset = parts.find { it.startsWith("charset=", ignoreCase = true) }
                    ?.substringAfter("=")?.trim()

                val inputStream = try {
                    if (code in 400..599) conn.errorStream ?: ByteArrayInputStream(ByteArray(0))
                    else conn.inputStream
                } catch (_: Exception) {
                    ByteArrayInputStream(ByteArray(0))
                }

                val responseHeaders = mutableMapOf<String, String>()
                conn.headerFields?.forEach { (key, values) ->
                    if (key != null && values.isNotEmpty()) {
                        if (key.equals("Content-Encoding", ignoreCase = true)) return@forEach
                        if (key.equals("Content-Length", ignoreCase = true)) return@forEach
                        responseHeaders[key] = values.last()
                    }
                }

                WebResourceResponse(mime, charset ?: encoding, code, message, responseHeaders, inputStream)
            } catch (e: Exception) {
                null
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
            if (code >= 400) mainFrameHttpError = true
            if (code == 404 || code == 410) {
                mainFrameFailed = true
                onMainFrameFailure()
            }
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.cancel()
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

        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
            if (detail?.didCrash() == true) {
                view?.destroy()
                recreate()
            } else {
                finish()
            }
            return true
        }
    }

    private fun onMainFrameFailure() {
        if (routingAway) return
        if (hadSuccessfulPage) {
            return
        }
        routingAway = true
        store.markLastUrlBroken()
        if (!resumeMode) {
            openApp(null)
            return
        }
        lifecycleScope.launch {
            when (val d = router.reprobeInitialEntry()) {
                is Destination.Web -> {
                    routingAway = false
                    resumeMode = false
                    webView.stopLoading()
                    webView.clearHistory()
                    webView.loadUrl(d.url, deviceHeaders)
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

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
            File.createTempFile("IMG_${timeStamp}_", ".jpg", dir)
        } catch (e: Exception) {
            null
        }
    }

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

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (::webView.isInitialized) webView.onPause()
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
