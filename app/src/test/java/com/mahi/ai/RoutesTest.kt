package com.mahi.ai

import com.mahi.ai.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {
    @Test fun routesAreDistinct() {
        val all = listOf(Routes.HOME, Routes.SETTINGS, Routes.CHAT, Routes.VOICE, Routes.CODING, Routes.MEMORY)
        assertEquals(all.size, all.toSet().size)
    }
}
