package com.nova.assistant.apps.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager

    private val commonPackageMap = mapOf(
        "whatsapp" to "com.whatsapp",
        "youtube" to "com.google.android.youtube",
        "chrome" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "instagram" to "com.instagram.android",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "play store" to "com.android.vending",
        "google play" to "com.android.vending",
        "spotify" to "com.spotify.music",
        "telegram" to "org.telegram.messenger",
        "twitter" to "com.twitter.android",
        "x" to "com.twitter.android",
        "facebook" to "com.facebook.katana"
    )

    fun launchAppByName(rawName: String): Result<String> {
        val appName = rawName.lowercase(Locale.ROOT).trim()

        // 1. Built-in system app shortcuts
        when (appName) {
            "settings" -> {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                return Result.success("Opening Settings")
            }
            "camera" -> {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                    return Result.success("Opening Camera")
                }
            }
            "gallery", "photos" -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                    return Result.success("Opening Gallery")
                }
            }
            "calculator" -> {
                val intent = Intent().apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                    return Result.success("Opening Calculator")
                }
            }
            "phone", "dialer" -> {
                val intent = Intent(Intent.ACTION_DIAL).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                context.startActivity(intent)
                return Result.success("Opening Phone")
            }
            "messages", "sms", "messaging" -> {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MESSAGING)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                    return Result.success("Opening Messages")
                }
            }
        }

        // 2. Lookup alias map
        val packageFromAlias = commonPackageMap[appName]
        if (packageFromAlias != null) {
            return launchPackage(packageFromAlias, rawName)
        }

        // 3. Search across installed launcher activities
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        for (info in resolveInfoList) {
            val label = info.loadLabel(packageManager).toString().lowercase(Locale.ROOT)
            if (label == appName || label.contains(appName)) {
                val pkg = info.activityInfo.packageName
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return Result.success("Opening ${info.loadLabel(packageManager)}")
                }
            }
        }

        return Result.failure(IllegalStateException("$rawName is not installed on this device"))
    }

    private fun launchPackage(packageName: String, displayName: String): Result<String> {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (launchIntent != null) {
            context.startActivity(launchIntent)
            Result.success("Opening $displayName")
        } else {
            Result.failure(IllegalStateException("$displayName is not installed on this device"))
        }
    }
}
