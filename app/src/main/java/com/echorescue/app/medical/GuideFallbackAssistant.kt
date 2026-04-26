package com.echorescue.app.medical

import com.echorescue.app.data.GuideRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GuideFallbackAssistant(
    private val guideRepository: GuideRepository
) : MedicalAssistant {
    override suspend fun availability(): AssistantAvailability = AssistantAvailability.FallbackOnly

    override suspend fun answer(question: String): Flow<String> = flow {
        val guide = guideRepository.findBestGuide(question)
        val text = "Offline guide match: ${guide.title}\n\n${guide.body}"
        for (char in text) {
            emit(char.toString())
            delay(8)
        }
    }
}
