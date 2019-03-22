package com.dbrain.textrecognizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Drawer
import com.dbrain.recognition.utils.dp

class TextDrawer(context: Context) : Drawer() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(4f, context)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val lineLength = dp(24f, context)
    private var points = FloatArray(32)
    private val colorRed = ContextCompat.getColor(context, R.color.app_red)
    private val colorGreen = ContextCompat.getColor(context, R.color.app_green)

    var detected = false

    override fun draw(cropRegionWidth: Int, cropRegionHeight: Int, canvas: Canvas?) {
        if (canvas == null || cropRegionHeight == 0 || cropRegionWidth == 0) return

        val canvasW = canvas.width
        val canvasH = canvas.height
        val left = canvasW / 2f - cropRegionWidth / 2f
        val right = left + cropRegionWidth
        val top = canvasH / 2f - cropRegionHeight / 2f
        val bottom = top + cropRegionHeight

        points = FloatArray(32)

        points[0] = left
        points[1] = top
        points[2] = left
        points[3] = top + lineLength

        points[4] = left
        points[5] = top
        points[6] = left + lineLength
        points[7] = top

        points[8] = left
        points[9] = bottom
        points[10] = left
        points[11] = bottom - lineLength

        points[12] = left
        points[13] = bottom
        points[14] = left + lineLength
        points[15] = bottom

        points[16] = right
        points[17] = top
        points[18] = right - lineLength
        points[19] = top

        points[20] = right
        points[21] = top
        points[22] = right
        points[23] = top + lineLength

        points[24] = right
        points[25] = bottom
        points[26] = right - lineLength
        points[27] = bottom

        points[28] = right
        points[29] = bottom
        points[30] = right
        points[31] = bottom - lineLength

        paint.color = if (detected) colorGreen else colorRed
        canvas.drawLines(points, paint)
    }

    override fun receiveEvent(data: DataBundle) {
        detected = data.detected
    }

}