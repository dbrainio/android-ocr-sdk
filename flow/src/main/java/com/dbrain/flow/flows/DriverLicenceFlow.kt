package com.dbrain.flow.flows

import android.util.Size


open class DriverLicenceFlow : FlowType(
    name = NAME,
    docType = DOC_TYPES,
    bordersAspectRatio = Size(85, 54),
    bordersSizeMultiplier = 0.9f
) {
    companion object {
        const val NAME = "driver_licence_flow"
        val DOC_TYPES = arrayOf(
            "driver_license_1999_paper_back", "driver_license_1999_paper_front",
            "driver_license_1999_plastic_back", "driver_license_1999_plastic_front",
            "driver_license_2011_back", "driver_license_2011_front", "driver_license_2014_back",
            "driver_license_japan"
        )
    }
}