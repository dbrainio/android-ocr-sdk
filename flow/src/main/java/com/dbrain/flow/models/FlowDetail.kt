package com.dbrain.flow.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class FlowDetail(
    val loc: List<String>?,
    val msg: String,
    val type: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(loc)
        parcel.writeString(msg)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FlowDetail> {
        override fun createFromParcel(parcel: Parcel): FlowDetail {
            return FlowDetail(parcel)
        }

        override fun newArray(size: Int): Array<FlowDetail?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) : FlowDetail {
            val loc = json.optJSONArray("loc")?.run {
                val list = ArrayList<String>()
                for (i in 0 until length()) {
                    list.add(optString(i))
                }
                list
            }
            return FlowDetail(
                loc,
                json.optString("msg"),
                json.optString("type")
            )
        }
    }
}
