package com.intech.spins

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Build.VERSION_CODES.S
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
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

                Log.i("马超1", "替换失败的url:${request?.url.toString()}")

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