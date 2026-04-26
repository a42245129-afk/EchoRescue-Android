package com.echorescue.app.medical

import kotlinx.coroutines.flow.Flow

enum class AssistantAvailability {
    Ready,
    FallbackOnly
}

interface MedicalAssistant {
    suspend fun availability(): AssistantAvailability
    suspend fun answer(question: String): Flow<String>
}
