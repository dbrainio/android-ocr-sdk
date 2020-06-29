package com.dbrain.recognition.data

import android.os.Parcelable
import android.util.Log
import com.dbrain.recognition.api.Key
import com.dbrain.recognition.utils.isNullOrEmpty
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONObject

@Parcelize
data class RecognizedItem(val docType: String, val fields: List<RecognizedField>): Parcelable {

    constructor(json: JSONObject) : this(json.getString(Key.DOC_TYPE), parseFields(json.getJSONObject(Key.FIELDS)))

    companion object {

        private val order = arrayListOf(
            "surname",
            "first_name",
            "other_names",
            "date_of_birth",
            "series_and_number",
            "subdivision_code",
            "date_of_issue",
            "issuing_authority",
            "place_of_birth"
        )

        private val comparator = Comparator<RecognizedField> { field1: RecognizedField, field2: RecognizedField ->
            val index1 = order.indexOf(field1.fieldName)
            val index2 = order.indexOf(field2.fieldName)
            if (index1 == -1 || index2 == -1) {
                return@Comparator 0
            }
            return@Comparator index1 - index2
        }

        private const val MRZ = "mrz"

        fun parseFields(json: JSONObject) : List<RecognizedField> {
            val fields = arrayListOf<RecognizedField>()
            json.keys().forEach {
                val field = RecognizedField(it, json.getJSONObject(it))
                if (!field.text.isBlank() && !field.fieldName.toLowerCase().startsWith(MRZ)) {
                    fields.add(field)
                }
            }
            fields.sortWith(comparator)
            return fields
        }

        fun list(array: JSONArray?) : ArrayList<RecognizedItem> {
            val list = arrayListOf<RecognizedItem>()
            if (array.isNullOrEmpty()) {
                return list
            }
            for (i in 0 until array!!.length()) {
                list.add(RecognizedItem(array.getJSONObject(i)))
            }
            return list
        }
    }
}
