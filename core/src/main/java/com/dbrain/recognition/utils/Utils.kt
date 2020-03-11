package com.dbrain.recognition.utils

import android.content.Context
import android.graphics.Typeface
import android.util.DisplayMetrics

fun dp(dp: Float, context: Context): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun getAppTypeface(context: Context) = Typeface.createFromAsset(context.assets, "fonts/TTNorms-Medium.otf")


fun normalizeType(type: String) = type.replace("_", " ").split(" ").joinToString(" ") { it.capitalize() }