package com.dbrain.flow.flows

import android.os.Parcel
import android.os.Parcelable
import android.util.Size
import androidx.annotation.IntRange

open class FlowType(
    var docType: Array<String>? = arrayOf(),
    var mode: String? = "default",
    var withHitl: Boolean? = null,
    var hitlAsync: Boolean? = null,
    var hitlRequiredFields: Array<String>? = null,
    var hitlSla: String? = "night",
    @IntRange(from = 0)
    var gauss: Int? = null,
    @IntRange(from = 0, to = 100)
    var quality: Int? = 75,
    @IntRange(from = 0)
    var dpi: Int? = 300,
    var autoPdfRawImages: Boolean? = true,
    var pdfRawImages: Boolean? = true,
    var useInternalApi: Boolean? = null,
    var checkFake: Boolean? = null,
    var async: Boolean? = null,
    var simpleCropper: Boolean? = null,
    @IntRange(from = 1)
    var priority: Int = 1,
    var verifyFields: Map<String, String>? = null,

    var bordersAspectRatio: Size = Size(1, 1),
    var bordersSizeMultiplier: Float = 0.8f
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createStringArray(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.createStringArray(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readInt(),
        parcel.readSerializable() as HashMap<String, String>,
        Size(parcel.readInt(), parcel.readInt()),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringArray(docType)
        parcel.writeString(mode)
        parcel.writeValue(withHitl)
        parcel.writeValue(hitlAsync)
        parcel.writeStringArray(hitlRequiredFields)
        parcel.writeString(hitlSla)
        parcel.writeValue(gauss)
        parcel.writeValue(quality)
        parcel.writeValue(dpi)
        parcel.writeValue(autoPdfRawImages)
        parcel.writeValue(pdfRawImages)
        parcel.writeValue(useInternalApi)
        parcel.writeValue(checkFake)
        parcel.writeValue(async)
        parcel.writeValue(simpleCropper)
        parcel.writeInt(priority)
        parcel.writeSerializable(verifyFields as HashMap<String, String>)
        parcel.writeInt(bordersAspectRatio.width)
        parcel.writeInt(bordersAspectRatio.height)
        parcel.writeFloat(bordersSizeMultiplier)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FlowType> {
        override fun createFromParcel(parcel: Parcel): FlowType {
            return FlowType(parcel)
        }

        override fun newArray(size: Int): Array<FlowType?> {
            return arrayOfNulls(size)
        }
    }
}