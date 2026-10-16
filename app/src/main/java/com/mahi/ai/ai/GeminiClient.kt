package com.mahi.ai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

data class ChatMessage(val role: String, val text: String) // role: "user" or "model"

class GeminiException(message: String) : Exception(message)

object GeminiClient {
    private const val BASE = "https://generativelanguage.googleapis.com/v1beta/models/"

    fun buildBody(history: List<ChatMessage>): String {
        val contents = JSONArray()
        history.forEach { m ->
            contents.put(JSONObject().put("role", m.role)
                .put("parts", JSONArray().put(JSONObject().put("text", m.text))))
        }
        return JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", Persona.SYSTEM_PROMPT))))
            .put("contents", contents)
            .toString()
    }

    fun mapError(code: Int): String = when (code) {
        400 -> "Request rejected (400). Check the model name in Settings."
        401, 403 -> "API key was not accepted. Check the key in Settings."
        404 -> "Model not found. Change the model name in Settings."
        429 -> "Free quota or rate limit reached. Wait a bit and try again."
        in 500..599 -> "Gemini server problem. Try again later."
        else -> "Unexpected error ($code)."
    }

    fun parseReply(json: String): String {
        val parts = JSONObject(json).optJSONArray("candidates")?.optJSONObject(0)
            ?.optJSONObject("content")?.optJSONArray("parts")
        val sb = StringBuilder()
        if (parts != null) for (i in 0 until parts.length()) sb.append(parts.getJSONObject(i).optString("text"))
        if (sb.isBlank()) throw GeminiException("Empty reply (the answer may have been blocked).")
        return sb.toString().trim()
    }

    /** One request only, no automatic retries. */
    suspend fun generate(apiKey: String, model: String, history: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            val conn = URL("$BASE$model:generateContent").openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.connectTimeout = 15_000
                conn.readTimeout = 45_000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("x-goog-api-key", apiKey)
                conn.outputStream.use { it.write(buildBody(history).toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                if (code != 200) throw GeminiException(mapError(code))
                parseReply(conn.inputStream.bufferedReader().use { it.readText() })
            } catch (e: SocketTimeoutException) {
                throw GeminiException("Request timed out.")
            } catch (e: IOException) {
                throw GeminiException("No connection. Check your internet.")
            } finally {
                conn.disconnect()
            }
        }
}
