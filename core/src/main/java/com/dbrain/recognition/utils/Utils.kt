package com.dbrain.recognition.utils

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.DisplayMetrics
import com.dbrain.recognition.R
import org.json.JSONArray

fun Context.getColorCompat(resId: Int): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return this.getColor(resId)
    }
    return this.resources.getColor(resId)
}

fun dp(dp: Float, context: Context): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun getAppTypeface(context: Context) = Typeface.createFromAsset(context.assets, "fonts/TTNorms-Medium.otf")


fun normalizeType(type: String) = type.replace("_", " ").split(" ").joinToString(" ") { it.capitalize() }

fun JSONArray?.isNullOrEmpty() = this == null || this.length() == 0