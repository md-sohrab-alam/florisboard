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

import android.content.Context
import dev.patrickgold.florisboard.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main AI engine that coordinates grammar and spelling correction.
 */
class AiEngine(
    private val aiService: AiService,
) {
    /**
     * Gets grammar and spelling correction for the given text.
     *
     * @param text The text to correct.
     * @return Result containing the corrected text or error information.
     */
    suspend fun getCorrection(text: String): AiCorrectionResult = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext AiCorrectionResult.success(text, text)
        }

        try {
            val correctedText = aiService.correctText(text)
            if (correctedText != null) {
                AiCorrectionResult.success(text, correctedText)
            } else {
                AiCorrectionResult.failure(text, "AI service returned null response")
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error occurred"
            AiCorrectionResult.failure(text, errorMessage)
        }
    }

    companion object {
        /**
         * Creates an AI engine instance using OpenAI service.
         * Reads the API key from BuildConfig, which is populated from local.properties at build time.
         *
         * @param context Android context.
         * @return AiEngine instance, or null if API key is not available.
         */
        fun create(context: Context): AiEngine? {
            // API key is read from BuildConfig, which is populated from local.properties at build time
            val apiKey = BuildConfig.OPENAI_API_KEY
            if (apiKey.isBlank()) {
                return null
            }
            val aiService = OpenAiService(apiKey)
            return AiEngine(aiService)
        }
    }
}

