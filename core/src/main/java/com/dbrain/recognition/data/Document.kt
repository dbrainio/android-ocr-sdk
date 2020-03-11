package com.dbrain.recognition.data

import android.os.Parcelable
import com.dbrain.recognition.api.Key
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

@Parcelize
data class Document(
    val type: String,
    val rotation: Int
    // val coords: Array<IntArray>
): Parcelable {
    constructor(json: JSONObject) : this(json.getString(Key.TYPE), json.getInt(Key.ROTATION))
}