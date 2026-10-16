package com.mahi.ai.command

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.mahi.ai.launcher.AppLauncher
import java.net.URLEncoder

sealed interface AssistantAction {
    data class OpenApp(val name: String) : AssistantAction
    data class YouTubeSearch(val query: String) : AssistantAction
    object OpenSettings : AssistantAction
}

object CommandParser {
    private val wake = Regex("^(hey\\s+)?mahi[,:]?\\s+", RegexOption.IGNORE_CASE)
    private val search = Regex("^(?:play|search(?: for)?|youtube search(?: for)?)\\s+(.+)$")
    private val open = Regex("^open\\s+(.+)$")
    private val bnOpen = Regex("^(.+?)\\s*(খোল|খুলো|ওপেন করো|চালু করো)$")
    private val bnPlay = Regex("^(.+?)\\s*(বাজাও|চালাও)$")

    fun parse(input: String): AssistantAction? {
        val t = input.trim().trimEnd('.', '!', '?', '।').replace(wake, "").trim()
        if (t.isEmpty()) return null
        val l = t.lowercase()
        if (l in setOf("open settings", "open android settings")) return AssistantAction.OpenSettings
        search.matchEntire(l)?.let { return AssistantAction.YouTubeSearch(it.groupValues[1].trim()) }
        open.matchEntire(l)?.let { return AssistantAction.OpenApp(it.groupValues[1].trim()) }
        bnPlay.matchEntire(t)?.let { return AssistantAction.YouTubeSearch(it.groupValues[1].trim()) }
        bnOpen.matchEntire(t)?.let { return AssistantAction.OpenApp(it.groupValues[1].trim()) }
        return null
    }
}

/** Strict allowlist: anything not listed here is never executed. */
object ActionValidator {
    fun isAllowed(a: AssistantAction): Boolean = when (a) {
        is AssistantAction.OpenApp -> a.name.length in 1..60
        is AssistantAction.YouTubeSearch -> a.query.length in 1..120
        AssistantAction.OpenSettings -> true
    }

    fun describe(a: AssistantAction): String = when (a) {
        is AssistantAction.OpenApp -> "Open app \"${a.name}\""
        is AssistantAction.YouTubeSearch -> "Search YouTube for \"${a.query}\""
        AssistantAction.OpenSettings -> "Open Android Settings"
    }
}

class ActionExecutor(private val ctx: Context, private val launcher: AppLauncher) {
    private val aliases = mapOf("ইউটিউব" to "youtube", "ক্রোম" to "chrome", "সেটিংস" to "settings")

    fun execute(a: AssistantAction): String {
        if (!ActionValidator.isAllowed(a)) return "That action isn't allowed."
        return try {
            when (a) {
                is AssistantAction.OpenApp -> {
                    val app = launcher.find(aliases[a.name] ?: a.name)
                        ?: return "I couldn't find an app called \"${a.name}\"."
                    val i = launcher.launchIntent(app.pkg) ?: return "${app.label} can't be launched."
                    ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    "Opening ${app.label}."
                }
                is AssistantAction.YouTubeSearch -> {
                    val url = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(a.query, "UTF-8")
                    val base = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        ctx.startActivity(Intent(base).setPackage("com.google.android.youtube"))
                    } catch (e: ActivityNotFoundException) {
                        ctx.startActivity(base) // browser fallback
                    }
                    "Searching YouTube for \"${a.query}\"."
                }
                AssistantAction.OpenSettings -> {
                    ctx.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    "Opening Settings."
                }
            }
        } catch (e: Exception) {
            "Couldn't do that: ${e.message}"
        }
    }
}
