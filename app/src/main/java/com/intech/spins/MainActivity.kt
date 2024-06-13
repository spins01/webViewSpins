package com.intech.spins

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.gyf.immersionbar.ImmersionBar
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var ivStartUp: RelativeLayout
    private lateinit var fullScreen: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var fullScreenContainer: FrameLayout
//    private var url = "https://spins777.vip"
    private var url = BuildConfig.DOMAIN
//    private var url = "https://spinsph.com/"
private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DensityUtils.setDensity(application, this@MainActivity)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        webView = findViewById<WebView>(R.id.webView)
        ivStartUp = findViewById<RelativeLayout>(R.id.ivStartUp)
        fullScreen = findViewById<FrameLayout>(R.id.fullScreen)
        ImmersionBar.with(this).statusBarDarkFont(true).init()
        // 在Activity的onCreate()方法中
        // 开启硬件加速
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        initLayoutParams()
        initWebView()
        //集成firebase推送
        firebasePush()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) {
                    customViewCallback?.onCustomViewHidden()
                    fullScreenContainer.removeView(customView)
                    customView = null
                    setContentView(R.layout.activity_main)
                } else if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {

        }
    }

    private fun askNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                       Toast.makeText(this,"You have disabled the POST_NOTIFICATIONS permission, so you will not receive notifications from the app. If needed, you can enable it manually.",Toast.LENGTH_LONG).show()
            } else {

                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    private fun firebasePush() {
        askNotificationPermission()
        //初始化Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //自定义事件,上传域名
        val bundleEvent = Bundle()
        bundleEvent.putString(FirebaseAnalytics.Param.METHOD, BuildConfig.DOMAIN)
        mFirebaseAnalytics?.logEvent("openUrl",bundleEvent)
//        FirebaseMessaging.getInstance().getToken()
//            .addOnCompleteListener { task ->
//                if (!task.isSuccessful()) {
//                    Log.w("张飞", "Fetching FCM registration token failed", task.getException())
//                    return@addOnCompleteListener
//                }
//                // Get new FCM registration token
//                val token: String = task.getResult()
//
//                // Log and toast
//                Log.d("张飞", "FCM Token: $token")
//            }

        Firebase.messaging.subscribeToTopic("weather")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("张飞", msg)
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            displayZoomControls = false // 隐藏缩放控件
            cacheMode = WebSettings.LOAD_DEFAULT // 启用默认缓存策略
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mediaPlaybackRequiresUserGesture = false
            }
            setGeolocationEnabled(true)
            allowContentAccess = true

        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback) {
                if (customView != null) {
                    callback.onCustomViewHidden()
                    return
                }
                fullScreen.visibility = View.VISIBLE
                webView.visibility = View.GONE
                fullScreen.addView(
                    view, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
                enterFullScreenMode()
            }

            override fun onHideCustomView() {
                if (webView.visibility === View.GONE) {
                    webView.visibility = View.VISIBLE
                }
                fullScreen.visibility = View.GONE
                exitFullScreenMode()
            }
        }
        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("ResourceType")
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                var inputStream: InputStream? = null
                if (request?.url.toString().endsWith("animate-6.f4ad1774.webp")) {
                    Log.i("马超11", "替换成功的url:${request?.url.toString()}")
                    try {
//                        inputStream = this@MainActivity.assets.open("ic_launcher.webp")
                        inputStream = this@MainActivity.assets.open("left.webp")
                    } catch (e: Exception) {
                        Log.i("马超11", "${e}       替换异常的url:${request?.url.toString()}")
                    }

                    return WebResourceResponse("image/webp", "UTF-8", inputStream)
                }
                if (request?.url.toString().endsWith("animate-1.918a6fa2.webp")) {
                    Log.i("马超11", "替换成功的url:${request?.url.toString()}")
                    try {
                        inputStream = this@MainActivity.assets.open("center.webp")
//                        inputStream = this@MainActivity.assets.open("ic_launcher.webp")
                    } catch (e: Exception) {
                        Log.i("马超11", "${e}       替换异常的url:${request?.url.toString()}")
                    }

                    return WebResourceResponse("image/webp", "UTF-8", inputStream)
                }
                if (request?.url.toString().endsWith("animate-2.169d751a.webp")) {
                    Log.i("马超11", "替换成功的url:${request?.url.toString()}")
                    try {
                        inputStream = this@MainActivity.assets.open("right.webp")
//                        inputStream = this@MainActivity.assets.open("ic_launcher.webp")
                    } catch (e: Exception) {
                        Log.i("马超11", "${e}       替换异常的url:${request?.url.toString()}")
                    }

                    return WebResourceResponse("image/webp", "UTF-8", inputStream)
                }


                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                ivStartUp.visibility = View.GONE
            }
        }
        webView.loadUrl(url)
    }

    @SuppressLint("ResourceType")
    private fun replaceUrl(picUrl: String): WebResourceResponse? {
        if (url.endsWith("static/img/animate-6.f4ad1774.webp")) {
            Log.i("马超1", "替换成功的url:${picUrl}")
            val inputStream = resources.openRawResource(R.mipmap.ic_launcher);
            return WebResourceResponse("image/webp", "UTF-8", inputStream)
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.apply {
            clearHistory()
            clearCache(true)
            removeAllViews()
            destroy()
        }
    }

    @Suppress("DEPRECATION")
    private fun enterFullScreenMode() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }

    @Suppress("DEPRECATION")
    private fun exitFullScreenMode() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun initLayoutParams() {
        val webViewLayoutParams = webView.layoutParams as ConstraintLayout.LayoutParams
        webViewLayoutParams.topMargin = ImmersionBar.getStatusBarHeight(this@MainActivity)
        val ivLayoutParams = ivStartUp.layoutParams as ConstraintLayout.LayoutParams
        ivLayoutParams.topMargin = ImmersionBar.getStatusBarHeight(this@MainActivity)
    }
}