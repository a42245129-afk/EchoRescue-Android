package com.echorescue.app.localization

import kotlin.math.abs
import kotlin.math.sqrt

object TrilaterationSolver {
    fun solve(measurements: List<AnchorMeasurement>): VictimEstimate? {
        if (measurements.size < 3) return null

        val reference = measurements.first()
        val rows = measurements.drop(1).map { measurement ->
            val x1 = reference.anchor.position.xMeters
            val y1 = reference.anchor.position.yMeters
            val xi = measurement.anchor.position.xMeters
            val yi = measurement.anchor.position.yMeters
            val r1 = reference.distanceMeters
            val ri = measurement.distanceMeters

            val a = 2.0 * (xi - x1)
            val b = 2.0 * (yi - y1)
            val d = (r1 * r1 - ri * ri) - (x1 * x1 - xi * xi) - (y1 * y1 - yi * yi)
            Triple(a, b, d)
        }

        if (rows.size < 2) return null

        val first = rows[0]
        val second = rows[1]
        val determinant = first.first * second.second - second.first * first.second
        if (abs(determinant) < 1e-6) return null

        val x = (first.third * second.second - second.third * first.second) / determinant
        val y = (first.first * second.third - second.first * first.third) / determinant

        val residuals = measurements.map { measurement ->
            val dx = x - measurement.anchor.position.xMeters
            val dy = y - measurement.anchor.position.yMeters
            abs(sqrt(dx * dx + dy * dy) - measurement.distanceMeters)
        }

        return VictimEstimate(
            position = Point2D(x, y),
            uncertaintyMeters = residuals.average().coerceAtLeast(0.1)
        )
    }
}
