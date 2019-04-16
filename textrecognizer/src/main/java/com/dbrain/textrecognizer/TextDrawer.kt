package com.dbrain.textrecognizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.Log
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Drawer

class TextDrawer(context: Context) : Drawer() {

    private val mask = Path()

    override fun draw(cropRegionWidth: Int, cropRegionHeight: Int, canvas: Canvas?) {
        if (canvas == null || cropRegionHeight == 0 || cropRegionWidth == 0) return

        val canvasWidth = canvas.width
        val canvasHeight = canvas.height

        mask.reset()

        Log.d("draw", "cropRegionWidth = $cropRegionWidth, cropRegionHeight = $cropRegionHeight, canvasWidth = $canvasWidth, canvasHeight = $canvasHeight")
    }

    override fun receiveEvent(data: DataBundle) = Unit

}