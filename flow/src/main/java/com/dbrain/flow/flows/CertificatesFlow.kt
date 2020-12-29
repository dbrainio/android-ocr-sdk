package com.dbrain.flow.flows

import android.util.Size


open class CertificatesFlow : FlowType(
    name = NAME,
    docType = DOC_TYPES,
    bordersAspectRatio = Size(71, 99),
    bordersSizeMultiplier = 0.9f
) {
    companion object {
        const val NAME = "cert_flow"
        val DOC_TYPES = arrayOf(
            "birth_certificate","marriage_certificate","divorce_certificate",
            "death_certificate"
        )
    }
}