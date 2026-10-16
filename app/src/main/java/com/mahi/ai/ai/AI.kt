package com.mahi.ai.ai

import com.mahi.ai.data.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ChatTurn(val role: String, val text: String)

interface AIProvider {
    suspend fun chat(history: List<ChatTurn>, system: String, model: String): Result<String>
    suspend fun health(): Result<String>
}

object PersonaPrompt {
    fun build(s: Settings): String {
        val address = if (s.useNickname && s.nickname.isNotBlank())
            "You may affectionately address the user as \"${s.nickname}\"." else ""
        return """
You are Mahi, a fictional female AI companion: warm, caring, playful, intelligent and respectful.
Reply in the same language the user writes in (Bangla or English). Keep replies conversational and concise.
$address
You are an AI. Never claim to be human or to have a physical presence. Do not encourage emotional dependency;
respect the user's boundaries and privacy. You can also help with coding and productivity.
Never ask for passwords, OTPs or payment details.
""".trimIndent()
    }
}

/** Talks to the Firebase Cloud Function; the Gemini key never lives in the app. */
class GeminiBackendProvider(private val settings: suspend () -> Settings) : AIProvider {

    override suspend fun chat(history: List<ChatTurn>, system: String, model: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val s = settings()
                require(s.backendUrl.isNotBlank()) { "Backend URL is not set. Open Settings." }
                val msgs = JSONArray()
                history.forEach { msgs.put(JSONObject().put("role", it.role).put("text", it.text)) }
                val body = JSONObject().put("model", model).put("system", system).put("messages", msgs)
                val resp = JSONObject(request("${s.backendUrl.trimEnd('/')}/chat", "POST", body.toString(), s.token))
                resp.optString("text").ifBlank { throw IOException("Empty response from AI.") }
            }
        }

    override suspend fun health(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val s = settings()
            require(s.backendUrl.isNotBlank()) { "Backend URL is not set." }
            request("${s.backendUrl.trimEnd('/')}/health", "GET", null, s.token)
            "Connected"
        }
    }

    private fun request(url: String, method: String, body: String?, token: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 15_000
            c.readTimeout = 60_000
            c.setRequestProperty("Content-Type", "application/json")
            if (token.isNotBlank()) c.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) { c.doOutput = true; c.outputStream.use { it.write(body.toByteArray()) } }
            val code = c.responseCode
            val text = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.readText().orEmpty()
            if (code !in 200..299) throw IOException(friendly(code))
            return text
        } finally { c.disconnect() }
    }

    private fun friendly(code: Int) = when (code) {
        401, 403 -> "Authentication failed. Check the app token in Settings."
        429 -> "Too many requests. Please wait a moment and retry."
        in 500..599 -> "AI server error ($code). Try again shortly."
        else -> "Request failed ($code)."
    }
}
