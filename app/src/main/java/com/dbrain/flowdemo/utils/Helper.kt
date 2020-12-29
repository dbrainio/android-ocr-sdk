package com.dbrain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.dbrain.flow.flows.*
import com.dbrain.flowdemo.R
import java.io.File
import kotlin.math.roundToInt

fun Context.getFlowTypesTitles() = arrayOf(
    getString(R.string.passport),
    getString(R.string.driver_licence),
    getString(R.string.car_documents),
    getString(R.string.personal_documents),
    getString(R.string.certificates),
    getString(R.string.other)
)

fun getFlowTypeByPosition(position: Int) = when (position) {
    0 -> PassportFlow()
    1 -> DriverLicenceFlow()
    2 -> VehicleFlow()
    3 -> PersonalDocumentsFlow()
    4 -> CertificatesFlow()
    else -> FlowType()
}

fun decodeBitmap(file: File) : Bitmap {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    options.inSampleSize = calculateInSampleSize(options, 700, 700)
    return BitmapFactory.decodeFile(file.absolutePath, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    val sWidth = (width.toFloat() / reqWidth.toFloat()).roundToInt()
    val sHeight = (height.toFloat() / reqHeight.toFloat()).roundToInt()
    return if (sWidth <= sHeight) sHeight else sWidth
}