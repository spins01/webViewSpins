package com.intech.spins

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.net.http.HttpResponseCache
import android.net.http.HttpResponseCache.install
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File

object InstallApkUtils {
    fun aa() {}
    fun requestInstallPermission(activity: MainActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES),
            2000
        )
    }

    fun installAPK(activity: MainActivity, apkPath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (activity.packageManager.canRequestPackageInstalls()) {
                installAbove(activity, apkPath)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.setData(Uri.parse("package:" + activity.packageName))
                activity.startActivityForResult(intent, 123456)
            }
        } else {
            installBelow(activity, apkPath)
        }
        // 检查安装权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
//            !activity.packageManager.canRequestPackageInstalls()) {
//            // 如果需要，请求安装权限
//            requestInstallPermission(activity)
//        } else {

//        }
    }

    fun installBelow(activity: MainActivity, apkPath: String) {
        // 安装 APK
        val apkUri = Uri.parse(apkPath)// 获取 APK 文件的 Uri
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(apkUri, "application/vnd.android.package-archive")
        }
        activity.startActivity(installIntent)
    }

    fun installAbove(activity: MainActivity, apkPath: String) {
        val apkFile = File(apkPath)
        val apkUri: Uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        activity.startActivity(installIntent)
    }

    fun install(activity: MainActivity, apkPath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            installAbove(activity, apkPath)
        } else {
            installBelow(activity, apkPath)
        }
    }
}
