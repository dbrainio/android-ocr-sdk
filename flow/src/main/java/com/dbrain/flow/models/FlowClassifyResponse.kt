package com.dbrain.flow.models

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class FlowClassifyResponse(
    val detail: List<FlowDetail>?,
    val items: List<FlowClassifyItem>?,
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
        parcel.createTypedArrayList(FlowClassifyItem),
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
        override fun createFromParcel(parcel: Parcel) = FlowRecognizeResponse(parcel)
        override fun newArray(size: Int) = arrayOfNulls<FlowRecognizeResponse>(size)

        fun parse(json: JSONObject) : FlowClassifyResponse {
            val details = json.optJSONArray("detail")?.run {
                val list = ArrayList<FlowDetail>()
                for (i in 0 until length()) {
                    list.add(FlowDetail.parse(optJSONObject(i)))
                }
                list
            }
            val items = json.optJSONArray("items")?.run {
                val list = ArrayList<FlowClassifyItem>()
                for (i in 0 until length()) {
                    list.add(FlowClassifyItem.parse(optJSONObject(i)))
                }
                list
            }
            return FlowClassifyResponse(
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

data class FlowClassifyItem(
    val document: FlowClassifyDocument?,
    val crop: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(FlowClassifyDocument::class.java.classLoader),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(document, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<FlowClassifyItem> {
        override fun createFromParcel(parcel: Parcel) = FlowClassifyItem(parcel)
        override fun newArray(size: Int) = arrayOfNulls<FlowClassifyItem>(size)

        fun parse(json: JSONObject) : FlowClassifyItem {
            return FlowClassifyItem(
                FlowClassifyDocument.parse(json.optJSONObject("document")),
                json.optString("crop")
            )
        }
    }

}

data class FlowClassifyDocument(
    val type: String,
    val page: Int,
    val rotation: Int,
    val coords: List<PointF>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(PointF.CREATOR)
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeInt(page)
        parcel.writeInt(rotation)
        parcel.writeTypedList(coords)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<FlowClassifyDocument> {
        override fun createFromParcel(parcel: Parcel) = FlowClassifyDocument(parcel)
        override fun newArray(size: Int) = arrayOfNulls<FlowClassifyDocument>(size)

        fun parse(json: JSONObject?) : FlowClassifyDocument? {
            json ?: return null

            val coords = json.optJSONArray("coords")?.run {
                val list = ArrayList<PointF>()
                for (i in 0 until length()) {
                    val arr = optJSONArray(i)
                    list.add(PointF(arr.optDouble(0).toFloat(),arr.optDouble(1).toFloat()))
                }
                list
            }

            return FlowClassifyDocument(
                json.optString("type"),
                json.optInt("page"),
                json.optInt("rotation"),
                coords
            )
        }
    }

}

