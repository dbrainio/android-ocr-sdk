package com.dbrain.flow.flows

import android.util.Size


open class DriverLicenceFlow : FlowType(
    docType = arrayOf(
        "driver_license_1999_paper_back", "driver_license_1999_paper_front",
        "driver_license_1999_plastic_back", "driver_license_1999_plastic_front",
        "driver_license_2011_back", "driver_license_2011_front", "driver_license_2014_back",
        "driver_license_japan"
    ),
    bordersAspectRatio = Size(85, 54),
    bordersSizeMultiplier = 0.9f
)