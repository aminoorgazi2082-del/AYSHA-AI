package com.aysha.ai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiService : AIProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generate(prompt: String, apiKey: String, model: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(apiKey.isNotBlank()) { "Settings-এ Gemini API Key দিন।" }
                val modelName = model.ifBlank { "gemini-2.5-flash" }
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                val system = "You are AYSHA, a friendly, intelligent and polite AI assistant. Support Bengali and English. Reply in the user's language. Be helpful and natural. You are an AI, not a human."
                val parts = org.json.JSONArray().put(JSONObject().put("text", "$system\n\nUser: $prompt"))
                val contents = org.json.JSONArray().put(JSONObject().put("role", "user").put("parts", parts))
                val body = JSONObject().put("contents", contents).toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val message = try { JSONObject(raw).optJSONObject("error")?.optString("message") } catch (_: Exception) { null }
                        throw IOException("Gemini API ${response.code}: ${message ?: raw.take(250)}")
                    }
                    val candidates = JSONObject(raw).optJSONArray("candidates")
                        ?: throw IOException("Gemini response-এ candidates নেই।")
                    val responseParts = candidates.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                        ?: throw IOException("Gemini response format ভুল।")
                    buildString {
                        for (i in 0 until responseParts.length()) append(responseParts.optJSONObject(i)?.optString("text").orEmpty())
                    }.ifBlank { "কোনো উত্তর পাওয়া যায়নি।" }
                }
            }
        }
}
