package com.echorescue.app.medical

import kotlinx.coroutines.flow.Flow

class HybridMedicalAssistant(
    private val fallbackAssistant: GuideFallbackAssistant
) : MedicalAssistant {
    override suspend fun availability(): AssistantAvailability {
        return AssistantAvailability.FallbackOnly
    }

    override suspend fun answer(question: String): Flow<String> {
        return fallbackAssistant.answer(question)
    }
}
