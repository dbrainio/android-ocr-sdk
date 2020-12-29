package com.dbrain.flowdemo.adapters.holders

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dbrain.flowdemo.R
import com.dbrain.flowdemo.adapters.Item

class ResponseItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.response_item, parent, false)
) {
    private val header = itemView.findViewById<TextView>(R.id.header)
    private val text = itemView.findViewById<TextView>(R.id.text)
    private val accuracy = itemView.findViewById<TextView>(R.id.accuracy)

    fun bind(item: Item.ResponseItem) {
        val context = itemView.context

        header.text = parseName(context, item.item.name)
        text.text = item.item.text

        val confidence = item.item.confidence ?: 0.0
        val (accuracyColor, accuracyText) = when {
            confidence > 0.9 -> ContextCompat.getColor(
                context,
                R.color.confidence_high
            ) to context.getString(R.string.confidence_high)
            confidence > 0.5 -> ContextCompat.getColor(
                context,
                R.color.confidence_medium
            ) to context.getString(R.string.confidence_medium)
            else -> ContextCompat.getColor(
                context,
                R.color.confidence_low
            ) to context.getString(R.string.confidence_low)
        }
        accuracy.apply {
            text = accuracyText
            background?.setTint(accuracyColor)
        }
    }

    private fun parseName(context: Context, name: String): String {
        return when (name) {
            "date_of_birth" -> context.getString(R.string.date_of_birth)
            "date_of_issue" -> context.getString(R.string.date_of_issue)
            "first_name", "name" -> context.getString(R.string.first_name)
            "issuing_authority" -> context.getString(R.string.issuing_authority)
            "other_names", "patronymic" -> context.getString(R.string.other_names)
            "place_of_birth" -> context.getString(R.string.place_of_birth)
            "sex" -> context.getString(R.string.sex)
            "subdivision_code" -> context.getString(R.string.subdivision_code)
            "surname" -> context.getString(R.string.surname)
            "series_and_number" -> context.getString(R.string.series_and_number)
            "date_from" -> context.getString(R.string.date_from)
            "date_end" -> context.getString(R.string.date_end)
            "place_of_issue" -> context.getString(R.string.place_of_issue)
            "number" -> context.getString(R.string.number)
            "issuer" -> context.getString(R.string.issuer)
            "category" -> context.getString(R.string.category)
            "born_full_name" -> context.getString(R.string.born_full_name)
            "father_full_name" -> context.getString(R.string.father_full_name)
            "mother_full_name" -> context.getString(R.string.mother_full_name)
            "record_number" -> context.getString(R.string.record_number)
            "doc_series" -> context.getString(R.string.doc_series)
            "doc_number" -> context.getString(R.string.doc_number)
            "date_of_death" -> context.getString(R.string.date_of_death)
            "place_of_death" -> context.getString(R.string.place_of_death)
            "full_name" -> context.getString(R.string.full_name)
            "date_of_divorce" -> context.getString(R.string.date_of_divorce)
            "spouse_1_date_of_birth" -> context.getString(R.string.spouse_1_date_of_birth)
            "spouse_2_date_of_birth" -> context.getString(R.string.spouse_2_date_of_birth)
            "spouse_1_full_name" -> context.getString(R.string.spouse_1_full_name)
            "spouse_2_full_name" -> context.getString(R.string.spouse_2_full_name)
            "date_of_expiry" -> context.getString(R.string.date_of_expiry)
            "date_of_marriage" -> context.getString(R.string.date_of_marriage)
            "husband_name" -> context.getString(R.string.husband_name)
            "wife_name" -> context.getString(R.string.wife_name)
            "address" -> context.getString(R.string.address)
            "date" -> context.getString(R.string.date)
            "special_marks" -> context.getString(R.string.special_marks)
            "name_eng" -> context.getString(R.string.name_eng)
            "name_rus" -> context.getString(R.string.name_rus)
            "sex_eng" -> context.getString(R.string.sex_eng)
            "sex_rus" -> context.getString(R.string.sex_rus)
            "surname_eng" -> context.getString(R.string.surname_eng)
            "surname_rus" -> context.getString(R.string.surname_rus)
            "series" -> context.getString(R.string.series)
            "accident_circumstances" -> context.getString(R.string.accident_circumstances)
            "miscellaneous" -> context.getString(R.string.miscellaneous)
            "autonomous_moving" -> context.getString(R.string.autonomous_moving)
            "legal_name_rus" -> context.getString(R.string.legal_name_rus)
            "legal_name" -> context.getString(R.string.legal_name)
            "engine_model" -> context.getString(R.string.engine_model)
            "engine_number" -> context.getString(R.string.engine_number)
            "engine_volume" -> context.getString(R.string.engine_volume)
            "temporary_registration_term" -> context.getString(R.string.temporary_registration_term)
            "series_bottom" -> context.getString(R.string.series_bottom)
            "number_bottom" -> context.getString(R.string.number_bottom)
            "cabine" -> context.getString(R.string.cabine)
            "chassis" -> context.getString(R.string.chassis)
            "color" -> context.getString(R.string.color)
            "country" -> context.getString(R.string.country)
            "empty_mass" -> context.getString(R.string.empty_mass)
            "engine_power" -> context.getString(R.string.engine_power)
            "engine_type" -> context.getString(R.string.engine_type)
            "max_mass" -> context.getString(R.string.max_mass)
            "model" -> context.getString(R.string.model)
            "type" -> context.getString(R.string.type)
            "vin" -> context.getString(R.string.vin)
            "year" -> context.getString(R.string.year)
            "vehicle_owner" -> context.getString(R.string.vehicle_owner)
            else -> name
        }
    }
}