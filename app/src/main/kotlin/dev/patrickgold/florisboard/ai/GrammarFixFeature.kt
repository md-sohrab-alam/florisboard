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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Feature coordinator for grammar fix functionality.
 * Manages the AI engine and correction state.
 */
class GrammarFixFeature(
    private val context: Context,
) {
    private val aiEngine: AiEngine? = AiEngine.create(context)

    private val _correctionResult = MutableStateFlow<AiCorrectionResult?>(null)
    val correctionResult: StateFlow<AiCorrectionResult?> = _correctionResult.asStateFlow()

    val isAvailable: Boolean
        get() = aiEngine != null

    /**
     * Requests grammar correction for the given text.
     *
     * @param text The text to correct.
     */
    suspend fun requestCorrection(text: String) {
        if (aiEngine == null) {
            _correctionResult.value = AiCorrectionResult.failure(
                text,
                "AI service not available. Please set OPEN_AI_KEY environment variable.",
            )
            return
        }

        _correctionResult.value = aiEngine.getCorrection(text)
    }

    /**
     * Clears the current correction result.
     */
    fun clearResult() {
        _correctionResult.value = null
    }
}

