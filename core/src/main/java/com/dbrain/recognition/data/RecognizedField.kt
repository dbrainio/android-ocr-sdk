package com.dbrain.recognition.data

import android.os.Parcelable
import com.dbrain.recognition.api.Key
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

@Parcelize
class RecognizedField(
    val fieldName: String,
    val text: String,
    private val confidence: Double
): Parcelable {

    constructor(fieldName: String, json: JSONObject) : this(fieldName, json.getString(Key.TEXT), json.getDouble(Key.CONFIDENCE))

    fun getAccuracy(): Accuracy {
        if (confidence >= .6) {
            return Accuracy.HIGH
        } else if (confidence >= .4 && .6 > confidence) {
            return Accuracy.MEDIUM
        }
        return Accuracy.LOW
    }

    enum class Accuracy {
        LOW, MEDIUM, HIGH
    }
}
