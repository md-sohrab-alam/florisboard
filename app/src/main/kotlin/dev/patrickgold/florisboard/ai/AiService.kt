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
 * Interface for AI services that provide grammar and spelling correction.
 */
interface AiService {
    /**
     * Corrects grammar and spelling in the given text.
     *
     * @param text The text to correct.
     * @return The corrected text, or null if correction failed.
     */
    suspend fun correctText(text: String): String?
}

