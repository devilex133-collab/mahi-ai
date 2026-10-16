package com.mahi.ai.launcher

import android.content.Context
import android.content.Intent

data class AppInfo(val label: String, val pkg: String)

class AppLauncher(private val ctx: Context) {
    fun list(): List<AppInfo> {
        val pm = ctx.packageManager
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(i, 0)
            .map { AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }

    fun find(name: String): AppInfo? {
        val n = name.lowercase().trim()
        val all = list()
        return all.firstOrNull { it.label.lowercase() == n }
            ?: all.firstOrNull { it.label.lowercase().contains(n) }
    }

    fun launchIntent(pkg: String): Intent? = ctx.packageManager.getLaunchIntentForPackage(pkg)
}
