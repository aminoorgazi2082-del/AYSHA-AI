package com.aysha.ai.ai
interface AIProvider {
    suspend fun generate(prompt: String, apiKey: String, model: String): Result<String>
}
