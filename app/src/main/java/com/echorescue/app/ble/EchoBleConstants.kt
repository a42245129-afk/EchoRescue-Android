package com.echorescue.app.ble

import java.nio.charset.StandardCharsets
import java.util.UUID

object EchoBleConstants {
    const val VictimDisplayName = "ECHORESCUE_VICTIM"
    val ServiceUuid: UUID = UUID.fromString("34c2d5f0-6fd8-4fcb-9f07-50f6919dc001")
    val CommandCharacteristicUuid: UUID = UUID.fromString("34c2d5f0-6fd8-4fcb-9f07-50f6919dc002")
    val StatusCharacteristicUuid: UUID = UUID.fromString("34c2d5f0-6fd8-4fcb-9f07-50f6919dc003")

    val ArmCommand: ByteArray = "ARM".toByteArray(StandardCharsets.UTF_8)
}
