package com.mahi.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mahi.ai.ui.AppNav
import com.mahi.ai.ui.MahiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MahiTheme { AppNav() } }
    }
}
