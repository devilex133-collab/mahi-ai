package com.mahi.ai

import android.app.Application
import androidx.room.Room
import com.mahi.ai.ai.AIProvider
import com.mahi.ai.ai.GeminiBackendProvider
import com.mahi.ai.command.ActionExecutor
import com.mahi.ai.data.AppDb
import com.mahi.ai.data.Prefs
import com.mahi.ai.launcher.AppLauncher
import kotlinx.coroutines.flow.first

class MahiApplication : Application() {
    val db by lazy { Room.databaseBuilder(this, AppDb::class.java, "mahi.db").build() }
    val prefs by lazy { Prefs(this) }
    val provider: AIProvider by lazy { GeminiBackendProvider { prefs.flow.first() } }
    val launcher by lazy { AppLauncher(this) }
    val executor by lazy { ActionExecutor(this, launcher) }
}
