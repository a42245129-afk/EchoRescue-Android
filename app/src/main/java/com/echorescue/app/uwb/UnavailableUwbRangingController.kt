package com.echorescue.app.uwb

import com.echorescue.app.ranging.MeasurementResult

class UnavailableUwbRangingController : UwbRangingController {
    override val isAvailable: Boolean = false

    override suspend fun measure(): Result<MeasurementResult> {
        return Result.failure(IllegalStateException("UWB ranging is not configured in this build."))
    }
}
