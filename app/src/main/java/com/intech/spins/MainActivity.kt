package com.intech.spins

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.ValueCallback
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ImmersionBar.destroy
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var ivStartUp: RelativeLayout
    private lateinit var fullScreen: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var fullScreenContainer: FrameLayout

    //    private var url = "https://spins777.vip"
    private var spinsUrl = BuildConfig.DOMAIN

    //    private var url = "https://spinsph.com/"
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 1
    private val PERMISSION_REQUEST_CODE = 100
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
        //集成firebase推送，
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

        checkAndRequestPermissions()
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
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(
                    this,
                    "You have disabled the POST_NOTIFICATIONS permission, so you will not receive notifications from the app. If needed, you can enable it manually.",
                    Toast.LENGTH_LONG
                ).show()
            } else {

                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun firebasePush() {
        askNotificationPermission()
        val sharedPreferences = getSharedPreferences("Spins", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
        //初始化Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //自定义事件,上传域名
        if (isFirstLaunch) {
            val bundleEvent = Bundle()
            bundleEvent.putString(FirebaseAnalytics.Param.METHOD, BuildConfig.DOMAIN)
            mFirebaseAnalytics?.logEvent("openUrl", bundleEvent)
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        }
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


            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true

        }
        webView.addJavascriptInterface(WebAppInterface(this), "appClient")
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

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (mFilePathCallback != null) {
                    mFilePathCallback!!.onReceiveValue(null)
                }
                mFilePathCallback = filePathCallback

                val intent = fileChooserParams?.createIntent() as Intent
                try {
                    startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE)
                } catch (e: ActivityNotFoundException) {
                    mFilePathCallback = null
                    Toast.makeText(this@MainActivity, "Cannot open file chooser", Toast.LENGTH_LONG)
                        .show()
                    return false
                }
                return true
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

//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): Boolean {
//                val urlInner = request?.url.toString()
//                if (urlInner.startsWith("gcash://")) {
//                    try {
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlInner))
//                        startActivity(intent)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        // 提示用户没有安装目标应用
//                    }
//                    return true
//                }
//                return super.shouldOverrideUrlLoading(view, request)
//            }
        }
        webView.loadUrl(spinsUrl)
    }

    @SuppressLint("ResourceType")
    private fun replaceUrl(picUrl: String): WebResourceResponse? {
        if (spinsUrl.endsWith("static/img/animate-6.f4ad1774.webp")) {
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
        webViewLayoutParams.bottomMargin = ImmersionBar.getNavigationBarHeight(this@MainActivity)
        webViewLayoutParams.topMargin = ImmersionBar.getStatusBarHeight(this@MainActivity)
        val ivLayoutParams = ivStartUp.layoutParams as ConstraintLayout.LayoutParams
        ivLayoutParams.topMargin = ImmersionBar.getStatusBarHeight(this@MainActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (mFilePathCallback != null) {
                var results: Array<Uri>? = null
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val result: Uri? = data.data
                        if (result != null) {
                            results = arrayOf(result)
                        }
                    }
                }
                mFilePathCallback?.onReceiveValue(results)
                mFilePathCallback = null
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }

            if (deniedPermissions.isEmpty()) {
                // All permissions granted, do nothing
            } else {
                // Some permissions denied
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}