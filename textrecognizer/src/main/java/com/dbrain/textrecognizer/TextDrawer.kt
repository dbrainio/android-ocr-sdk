package com.dbrain.textrecognizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Drawer
import com.dbrain.recognition.utils.dp

class TextDrawer(context: Context) : Drawer() {

    private val mask = Path().apply {
        fillType = Path.FillType.EVEN_ODD
    }

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL_AND_STROKE
    }

    private var lastCanvasWidth = 0
    private var lastCanvasHeight = 0
    private var lastCropRegionWidth = 0
    private var lastCropRegionHeight = 0

    private val cornerRadius = dp(6f, context)

    private val padding = dp(1f, context)

    override fun draw(cropRegionWidth: Int, cropRegionHeight: Int, canvas: Canvas?) {
        if (canvas == null || cropRegionHeight == 0 || cropRegionWidth == 0) return

        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()

        val changed = cropRegionWidth != lastCropRegionWidth || cropRegionHeight != lastCropRegionHeight || lastCanvasWidth != canvas.width || lastCanvasHeight != canvas.height

        if (changed) {
            lastCanvasWidth = canvas.width
            lastCanvasHeight = canvas.height
            lastCropRegionWidth = cropRegionWidth
            lastCropRegionHeight = cropRegionHeight

            val cropRegionUsableHeight = cropRegionHeight * 0.7f

            mask.reset()

            mask.moveTo(0f, 0f)
            mask.lineTo(canvasWidth, 0f)
            mask.lineTo(canvasWidth, canvasHeight)
            mask.lineTo(0f, canvasHeight)
            mask.lineTo(0f, 0f)
            mask.close()

            val cropRegionCenterX = canvas.width / 2
            val cropRegionCenterY = canvas.height / 2

            val cropRegionLeft = cropRegionCenterX - cropRegionWidth / 2f
            val cropRegionTop = cropRegionCenterY - cropRegionHeight / 2f
            val cropRegionRight = cropRegionCenterX + cropRegionWidth / 2f
            val cropRegionBottom = cropRegionTop + cropRegionUsableHeight

            val firstRectTop = cropRegionTop
            val firstRectBottom = (cropRegionBottom + cropRegionTop) / 2 - padding
            val secondRectTop = (cropRegionBottom + cropRegionTop) / 2 + padding
            val secondRectBottom = cropRegionBottom

            mask.addRoundRect(cropRegionLeft, firstRectTop, cropRegionRight, firstRectBottom, cornerRadius, cornerRadius, Path.Direction.CCW)
            mask.close()

            mask.addRoundRect(cropRegionLeft, secondRectTop, cropRegionRight, secondRectBottom, cornerRadius, cornerRadius, Path.Direction.CCW)
            mask.close()

        }

        canvas.drawPath(mask, maskPaint)
    }

    override fun receiveEvent(data: DataBundle) = Unit

}