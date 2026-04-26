package com.echorescue.app.uwb

import com.echorescue.app.ranging.MeasurementResult

interface UwbRangingController {
    val isAvailable: Boolean
    suspend fun measure(): Result<MeasurementResult>
}
