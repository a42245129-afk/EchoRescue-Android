package com.echorescue.app.data

import kotlinx.serialization.Serializable

@Serializable
data class SurvivalGuide(
    val id: String,
    val title: String,
    val summary: String,
    val keywords: List<String>,
    val body: String
)
