/*
 * Copyright (C) 2025 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.ai

import dev.patrickgold.florisboard.lib.devtools.LogTopic
import dev.patrickgold.florisboard.lib.devtools.flogError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * OpenAI service implementation for grammar and spelling correction.
 */
class OpenAiService(
    private val apiKey: String,
) : AiService {
    companion object {
        private const val API_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL = "gpt-4o-mini"
        private const val SYSTEM_PROMPT = """You are a grammar and spelling correction assistant. 
Your task is to correct grammar, spelling, and improve clarity while preserving the original meaning and style.
Return ONLY the corrected text, without any explanations, prefixes, or additional commentary.
If the text is already correct, return it unchanged."""

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Serializable
    private data class ChatMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Double = 0.3,
        val max_tokens: Int = 1000,
    )

    @Serializable
    private data class ChatChoice(
        val message: ChatMessage,
    )

    @Serializable
    private data class ChatResponse(
        val choices: List<ChatChoice>? = null,
        val error: ErrorResponse? = null,
    )

    @Serializable
    private data class ErrorResponse(
        val message: String? = null,
    )

    override suspend fun correctText(text: String): String? = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext text
        }

        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 30000

            val request = ChatRequest(
                model = MODEL,
                messages = listOf(
                    ChatMessage(role = "system", content = SYSTEM_PROMPT),
                    ChatMessage(role = "user", content = text),
                ),
            )

            connection.outputStream.use { output ->
                output.write(json.encodeToString(ChatRequest.serializer(), request).toByteArray())
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = json.decodeFromString<ChatResponse>(responseBody)
                val correctedText = response.choices?.firstOrNull()?.message?.content?.trim()
                if (correctedText == null) {
                    flogError(LogTopic.OTHER) { "OpenAI API returned success but no corrected text in response: $responseBody" }
                    throw Exception("No corrected text in API response")
                }
                correctedText
            } else {
                val errorResponse = try {
                    json.decodeFromString<ChatResponse>(responseBody)
                } catch (e: Exception) {
                    null
                }
                val errorMessage = errorResponse?.error?.message ?: "HTTP $responseCode: $responseBody"
                flogError(LogTopic.OTHER) { "OpenAI API error: $errorMessage" }
                throw Exception(errorMessage)
            }
        } catch (e: IOException) {
            flogError(LogTopic.OTHER) { "OpenAI API network error: ${e.message}" }
            throw Exception("Network error: ${e.message}", e)
        } catch (e: Exception) {
            if (e.message?.startsWith("Network error:") != true && e.message?.startsWith("HTTP") != true) {
                flogError(LogTopic.OTHER) { "OpenAI API unexpected error: ${e.message}" }
            }
            throw e
        }
    }
}

