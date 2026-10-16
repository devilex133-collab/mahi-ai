package com.mahi.ai.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("mahi_prefs")

data class Settings(
    val backendUrl: String = "",
    val token: String = "",
    val model: String = "gemini-2.5-flash",
    val nickname: String = "Jan",
    val useNickname: Boolean = true,
    val speechLang: String = "bn-BD",
    val autoSpeak: Boolean = true,
    val speechRate: Float = 1f
)

class Prefs(private val ctx: Context) {
    private object K {
        val url = stringPreferencesKey("url")
        val token = stringPreferencesKey("token")
        val model = stringPreferencesKey("model")
        val nick = stringPreferencesKey("nick")
        val useNick = booleanPreferencesKey("use_nick")
        val lang = stringPreferencesKey("lang")
        val auto = booleanPreferencesKey("auto")
        val rate = floatPreferencesKey("rate")
    }

    val flow: Flow<Settings> = ctx.store.data.map { p ->
        val d = Settings()
        Settings(
            p[K.url] ?: d.backendUrl, p[K.token] ?: d.token, p[K.model] ?: d.model,
            p[K.nick] ?: d.nickname, p[K.useNick] ?: d.useNickname,
            p[K.lang] ?: d.speechLang, p[K.auto] ?: d.autoSpeak, p[K.rate] ?: d.speechRate
        )
    }

    suspend fun update(block: (Settings) -> Settings) {
        val n = block(flow.first())
        ctx.store.edit {
            it[K.url] = n.backendUrl; it[K.token] = n.token; it[K.model] = n.model
            it[K.nick] = n.nickname; it[K.useNick] = n.useNickname
            it[K.lang] = n.speechLang; it[K.auto] = n.autoSpeak; it[K.rate] = n.speechRate
        }
    }
}
