package com.dbrain.recognition.camera

import java.util.Locale

data class Size(
    val width: Int,
    val height: Int
) {

    override fun toString(): String {
        return String.format(Locale.getDefault(), "%dx%d", width, height)
    }
}