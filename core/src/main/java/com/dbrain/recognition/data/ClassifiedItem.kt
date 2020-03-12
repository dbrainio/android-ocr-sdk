package com.dbrain.recognition.data

import android.os.Parcelable
import com.dbrain.recognition.api.Key
import com.dbrain.recognition.utils.isNullOrEmpty
import com.dbrain.recognition.utils.normalizeType
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONObject

@Parcelize
data class ClassifiedItem(
    val document: Document
    // val crop: String
): Parcelable {
    constructor(json: JSONObject) : this(Document(json.getJSONObject(Key.DOCUMENT)))

    companion object {

        fun list(array: JSONArray?): ArrayList<ClassifiedItem> {
            val list = ArrayList<ClassifiedItem>()
            if (array.isNullOrEmpty()) {
                return list
            }
            for (i in 0 until array!!.length()) {
                list.add(ClassifiedItem(array.getJSONObject(i)))
            }
            return list
        }

        fun titles(array: List<ClassifiedItem>): Array<String> {
            val titles = ArrayList<String>()
            for (i in array.indices) {
                titles.add(normalizeType(array[i].document.type))
            }
            return titles.toTypedArray()
        }
    }
}