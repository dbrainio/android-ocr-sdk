package com.dbrain.flow.common

import android.content.Context
import android.content.res.Resources
import java.io.File

internal fun Context.getOutputDirectory(): File {
    val mediaDir = externalMediaDirs.firstOrNull()?.let { File(it, "DBrain").apply { mkdirs() } }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
}

internal fun dp(dp: Int): Float {
    return (dp * Resources.getSystem().displayMetrics.density)
}