package com.dbrain.flow.flows

import android.util.Size


open class VehicleFlow : FlowType(
    name = NAME,
    docType = DOC_TYPES,
    bordersAspectRatio = Size(71, 99),
    bordersSizeMultiplier = 0.9f
) {
    companion object {
        const val NAME = "vehicle_flow"
        val DOC_TYPES = arrayOf(
            "vehicle_registration_certificate_front","vehicle_registration_certificate_back",
            "pts_front","pts_back","traffic_accident_notice_front","traffic_accident_notice_back"
        )
    }
}