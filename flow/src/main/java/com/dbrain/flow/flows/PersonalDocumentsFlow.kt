package com.dbrain.flow.flows

import android.util.Size


open class PersonalDocumentsFlow : FlowType(
    name = NAME,
    docType = DOC_TYPES,
    bordersAspectRatio = Size(71, 99),
    bordersSizeMultiplier = 0.9f
) {
    companion object {
        const val NAME = "personal_docs_flow"
        val DOC_TYPES = arrayOf(
            "snils_front","insurance_plastic","inn_person","health_insurance_certificate_card_front",
            "health_insurance_certificate_card_back","health_insurance_certificate_paper_front",
            "health_insurance_certificate_moscow_card_front","bank_card",
            "migration_card","rus_work_patent","ndfl2","mts_rfa"
        )
    }
}