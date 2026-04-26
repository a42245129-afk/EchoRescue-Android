package com.echorescue.app.data

import android.content.Context
import kotlinx.serialization.json.Json

class GuideRepository(context: Context) {
    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedGuides: List<SurvivalGuide>? = null

    suspend fun getGuides(): List<SurvivalGuide> {
        cachedGuides?.let { return it }
        val content = appContext.assets.open("guides.json").bufferedReader().use { it.readText() }
        return json.decodeFromString<List<SurvivalGuide>>(content).also {
            cachedGuides = it
        }
    }

    suspend fun findBestGuide(question: String): SurvivalGuide {
        val guides = getGuides()
        val lowered = question.lowercase()
        return guides.maxByOrNull { guide ->
            var score = 0
            if (lowered.contains(guide.title.lowercase())) score += 6
            guide.keywords.forEach { keyword ->
                if (lowered.contains(keyword.lowercase())) score += 3
            }
            guide.summary.lowercase().split(" ").forEach { token ->
                if (token.isNotBlank() && lowered.contains(token)) score += 1
            }
            score
        } ?: guides.first()
    }
}
