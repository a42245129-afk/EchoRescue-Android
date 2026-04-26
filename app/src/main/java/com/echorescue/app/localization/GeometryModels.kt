package com.echorescue.app.localization

data class Point2D(
    val xMeters: Double,
    val yMeters: Double
)

data class AnchorPoint(
    val id: String,
    val label: String,
    val position: Point2D
)

data class AnchorMeasurement(
    val anchor: AnchorPoint,
    val distanceMeters: Double,
    val confidence: Int
)

data class VictimEstimate(
    val position: Point2D,
    val uncertaintyMeters: Double
)

object RescueGeometry {
    val defaultAnchors: List<AnchorPoint> = listOf(
        AnchorPoint("alpha", "Anchor A", Point2D(0.0, 0.0)),
        AnchorPoint("bravo", "Anchor B", Point2D(5.0, 0.0)),
        AnchorPoint("charlie", "Anchor C", Point2D(0.0, 5.0))
    )
}
