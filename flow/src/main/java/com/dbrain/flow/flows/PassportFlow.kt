package com.dbrain.flow.flows

import android.util.Size


open class PassportFlow : FlowType(
    name = NAME,
    docType = DOC_TYPES,
    bordersAspectRatio = Size(125, 175),
    bordersSizeMultiplier = 0.75f
) {
    companion object {
        const val NAME = "passport_flow"
        val DOC_TYPES = arrayOf(
            "kgz_passport_main", "kgz_passport_plastic_blue", "kgz_passport_plastic_red",
            "passport_blank_page", "passport_children", "passport_last_rf", "passport_main",
            "passport_main_handwritten", "passport_marriage", "passport_military",
            "passport_previous_docs","passport_registration","passport_zero_page")
    }
}