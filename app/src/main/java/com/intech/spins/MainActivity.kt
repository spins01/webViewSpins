package com.intech.spins

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.webbridge.AdjustBridge
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.Button
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rxhttp.toDownloadFlow
import rxhttp.wrapper.param.RxHttp
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
    private var mApkPath: String = ""
    private lateinit var progressDialog: AlertDialog
    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        DensityUtils.setDensity(application, this@MainActivity)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        checkAndRequestPermissions()
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
        firebaseButtonClicked()
        Adjust.onPause()
      /*  var connect = findViewById<android.widget.Button>(R.id.btConnect)
        var userId = findViewById<EditText>(R.id.etUserID)
        var roomId = findViewById<EditText>(R.id.etRoomID)*/
    /*    connect.setOnClickListener{
            webView.evaluateJavascript("javascript:name(${userId.text},${roomId.text})",null)
        }*/
    }

    private suspend fun download() {
        val flavor = BuildConfig.FLAVOR
        val path = "https://spinscasino.games/downloadApk/$flavor/app-$flavor-release.apk"
        RxHttp.get(path)
            .toDownloadFlow("${cacheDir}/app-$flavor-release.apk")
            .onProgress {
                progressBar.progress = it.progress
            }
            .onStart {
                progressDialog = showProgressDialog(this@MainActivity)
            }
            .onCompletion {
                progressDialog.dismiss()
            }
            .catch {
            }
            .collect {
                mApkPath = it
                InstallApkUtils.installAPK(this@MainActivity, it)
            }
    }

    private fun firebaseButtonClicked() {
        var remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)  // 一小时更新一次
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 获取版本号
                    val latestVersion = remoteConfig.getLong("latest_version")
                    if (latestVersion > BuildConfig.VERSION_CODE) {
                        AlertDialog.Builder(this@MainActivity).setTitle("Friendly Reminder")
                            .setMessage("Found the latest version. Would you like to update?")
                            .setPositiveButton("update", object : OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    lifecycleScope.launch {
                                        download()
                                    }
                                }

                            })
                            .setNeutralButton("cancel",object : OnClickListener{
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    dialog?.dismiss()
                                }

                            }).show()

                    }
                    Log.i("马超", "latestVersion$latestVersion")
                } else {
                    Log.i("马超", "latestVersion获取失败")
                }
            }

//        FirebaseInAppMessaging.getInstance()
//            .addClickListener { inAppMessage: InAppMessage, action: Action ->
//                val actionType = action.actionUrl
//                if (actionType == "https://spins.com/download") {
//                    lifecycleScope.launch {
//                        download()
//                    }
//                }
//            }
    }

    private fun firebasePush() {
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
                fullScreen.removeAllViews()
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
        AdjustBridge.registerAndGetInstance(application, webView)
        webView.loadUrl(spinsUrl)
    }


    override fun onDestroy() {
        super.onDestroy()
        webView.apply {
            clearHistory()
            clearCache(true)
            removeAllViews()
            destroy()
        }
        AdjustBridge.unregister()
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
        } else if (requestCode == 123456) {
            InstallApkUtils.install(this@MainActivity, mApkPath)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
//            Manifest.permission.REQUEST_INSTALL_PACKAGES
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
//        }else{
//            lifecycleScope.launch {
//                firebaseButtonClicked()
//            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {

            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // 获取被拒绝的权限
                    val deniedPermission = permissions[i]
                    // 处理被拒绝的权限，例如禁用相关功能或者展示进一步提示
                    handleDeniedPermission(deniedPermission)
                }
            }
        }
    }

    private fun handleDeniedPermission(deniedPermission: String) {
        when (deniedPermission) {
            Manifest.permission.POST_NOTIFICATIONS -> Snackbar.make(
                findViewById(android.R.id.content),
                "You have disabled the POST_NOTIFICATIONS permission, so you will not receive notifications from the app. If needed, you can enable it manually.",
                Toast.LENGTH_LONG
            ).show()

            Manifest.permission.READ_MEDIA_IMAGES -> Snackbar.make(
                findViewById(android.R.id.content),
                "You have disabled the READ_MEDIA_IMAGES permission,You won't be able to access the photos on your phone.",
                Toast.LENGTH_LONG
            ).show()

            Manifest.permission.READ_MEDIA_VIDEO -> Snackbar.make(
                findViewById(android.R.id.content),
                "You have disabled the READ_MEDIA_VIDEO permission,You won't be able to access the videos on your phone.",
                Toast.LENGTH_LONG
            ).show()

            Manifest.permission.READ_MEDIA_AUDIO -> Snackbar.make(
                findViewById(android.R.id.content),
                "You have disabled the READ_MEDIA_AUDIO permission,You won't be able to access the audios on your phone.",
                Toast.LENGTH_LONG
            ).show()

            Manifest.permission.REQUEST_INSTALL_PACKAGES -> {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "You have disabled the REQUEST_INSTALL_PACKAGES permission,You won't be able to install the latest apk",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    fun showProgressDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_progress, null)
        progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        builder.setView(view)
        builder.setCancelable(false) // 禁止点击外部或返回键取消弹窗

        val dialog = builder.create()
        dialog.show()

        return dialog
    }

    override fun onResume() {
        super.onResume()
        Adjust.onResume()
    }
}