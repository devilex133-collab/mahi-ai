package com.mahi.ai

import com.mahi.ai.ai.ChatMessage
import com.mahi.ai.ai.GeminiClient
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiClientTest {
    @Test fun errorMapping() {
        assertTrue(GeminiClient.mapError(429).contains("quota", ignoreCase = true))
        assertTrue(GeminiClient.mapError(403).contains("key", ignoreCase = true))
    }
    @Test fun bodyHasRolesAndSystem() {
        val body = GeminiClient.buildBody(listOf(ChatMessage("user", "hi")))
        assertTrue(body.contains("\"role\":\"user\"") && body.contains("systemInstruction"))
    }
}
