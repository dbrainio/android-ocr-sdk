package com.dbrain.recognition.data

import android.os.Parcelable
import com.dbrain.recognition.api.Key
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONObject

@Parcelize
data class RecognizedItem(val docType: String, val fields: List<RecognizedField>): Parcelable {

    constructor(json: JSONObject) : this(json.getString(Key.DOC_TYPE), parseFields(json.getJSONObject(Key.FIELDS)))

    companion object {

        fun parseFields(json: JSONObject) : List<RecognizedField> {
            val fields = arrayListOf<RecognizedField>()
            json.keys().forEach {
                fields.add(RecognizedField(it, json.getJSONObject(it)))
            }
            return fields
        }

        fun list(array: JSONArray) : ArrayList<RecognizedItem> {
            val list = arrayListOf<RecognizedItem>()
            for (i in 0 until array.length()) {
                list.add(RecognizedItem(array.getJSONObject(i)))
            }
            return list
        }
    }
}
