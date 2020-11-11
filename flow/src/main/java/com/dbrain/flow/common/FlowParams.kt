package com.dbrain.flow.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class FlowParams(
    val aspectW: Float = 1f,
    val aspectH: Float = 1f,
    val cropFactor: Float = 1f,
    val clippingThreshold: Float = 1f
) : Parcelable