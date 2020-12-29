package com.dbrain.flow.models

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class FlowRecognizeResponse(
    val detail: List<FlowDetail>?,
    val items: List<FlowRecognizeItem>?,
    val taskId: String?,
    val code: Int,
    val message: String?,
    val errno: Int,
    val traceback: String?,
    val fake: Boolean?,
    val pagesCount: Int,
    val docsCount: Int,
    val raw: String?
) : Response(code), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(FlowDetail),
        parcel.createTypedArrayList(FlowRecognizeItem),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(detail)
        parcel.writeTypedList(items)
        parcel.writeString(taskId)
        parcel.writeInt(code)
        parcel.writeString(message)
        parcel.writeInt(errno)
        parcel.writeString(traceback)
        parcel.writeValue(fake)
        parcel.writeInt(pagesCount)
        parcel.writeInt(docsCount)
        parcel.writeString(raw)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<FlowRecognizeResponse> {
        override fun createFromParcel(parcel: Parcel): FlowRecognizeResponse {
            return FlowRecognizeResponse(parcel)
        }

        override fun newArray(size: Int): Array<FlowRecognizeResponse?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) : FlowRecognizeResponse {
            val details = json.optJSONArray("detail")?.run {
                val list = ArrayList<FlowDetail>()
                for (i in 0 until length()) {
                    list.add(FlowDetail.parse(optJSONObject(i)))
                }
                list
            }
            val items = json.optJSONArray("items")?.run {
                val list = ArrayList<FlowRecognizeItem>()
                for (i in 0 until length()) {
                    list.add(FlowRecognizeItem.parse(optJSONObject(i)))
                }
                list
            }
            return FlowRecognizeResponse(
                details,
                items,
                json.optString("task_id"),
                json.optInt("code"),
                json.optString("message"),
                json.optInt("errno"),
                json.optString("traceback"),
                json.optBoolean("fake"),
                json.optInt("pages_count"),
                json.optInt("docs_count"),
                json.toString()
            )
        }
    }
}

data class FlowRecognizeItem(
    val docType: String,
    val fields: List<FlowRecognizeItemField>,
    val color: Boolean,
    val error: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createTypedArrayList(FlowRecognizeItemField) ?: arrayListOf(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(docType)
        parcel.writeTypedList(fields)
        parcel.writeByte(if (color) 1 else 0)
        parcel.writeString(error)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<FlowRecognizeItem> {
        override fun createFromParcel(parcel: Parcel) = FlowRecognizeItem(parcel)

        override fun newArray(size: Int) = arrayOfNulls<FlowRecognizeItem?>(size)

        fun parse(json: JSONObject) : FlowRecognizeItem {
            val fields = ArrayList<FlowRecognizeItemField>()
            json.optJSONObject("fields")?.let { jFields ->
                jFields.keys().forEach {
                    val jField = jFields.optJSONObject(it)
                    fields.add(FlowRecognizeItemField.parse(it, jField))
                }
            }
            return FlowRecognizeItem(
                json.optString("doc_type"),
                fields,
                json.optBoolean("color"),
                json.optString("error")
            )
        }
    }
}

data class FlowRecognizeItemField(
    val name: String,
    val text: String,
    val confidence: Double?,
    val valid: Boolean?,
    val coords: List<PointF>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.createTypedArrayList(PointF.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(text)
        parcel.writeValue(confidence)
        parcel.writeValue(valid)
        parcel.writeTypedList(coords)
    }

    override fun describeContents(): Int {
        return 0
    }

    internal companion object CREATOR : Parcelable.Creator<FlowRecognizeItemField> {
        override fun createFromParcel(parcel: Parcel): FlowRecognizeItemField {
            return FlowRecognizeItemField(parcel)
        }

        override fun newArray(size: Int): Array<FlowRecognizeItemField?> {
            return arrayOfNulls(size)
        }

        fun parse(name: String, json: JSONObject) : FlowRecognizeItemField {
            val coords = json.optJSONArray("coords")?.run {
                val list = ArrayList<PointF>()
                for (i in 0 until length()) {
                    val arr = optJSONArray(i)
                    list.add(PointF(arr.optDouble(0).toFloat(),arr.optDouble(1).toFloat()))
                }
                list
            }
            return FlowRecognizeItemField(
                name,
                json.optString("text"),
                json.optDouble("confidence"),
                if (!json.isNull("valid")) json.optBoolean("valid") else null,
                coords
            )
        }
    }

}
