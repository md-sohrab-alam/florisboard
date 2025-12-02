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

/**
 * Result of an AI grammar correction operation.
 *
 * @property originalText The original text that was sent for correction.
 * @property correctedText The corrected text returned by the AI service.
 * @property success Whether the correction was successful.
 * @property errorMessage Error message if correction failed, null otherwise.
 */
data class AiCorrectionResult(
    val originalText: String,
    val correctedText: String?,
    val success: Boolean,
    val errorMessage: String? = null,
) {
    companion object {
        fun success(originalText: String, correctedText: String): AiCorrectionResult {
            return AiCorrectionResult(
                originalText = originalText,
                correctedText = correctedText,
                success = true,
            )
        }

        fun failure(originalText: String, errorMessage: String): AiCorrectionResult {
            return AiCorrectionResult(
                originalText = originalText,
                correctedText = null,
                success = false,
                errorMessage = errorMessage,
            )
        }
    }
}

