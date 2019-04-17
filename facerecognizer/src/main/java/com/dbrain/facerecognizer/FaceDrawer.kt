package com.dbrain.facerecognizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Drawer
import com.dbrain.recognition.utils.dp


class FaceDrawer(context: Context) : Drawer() {

    private val mask = Path().apply {
        fillType = Path.FillType.EVEN_ODD
    }
    private val strokeFull = Path()
    private val strokeMeasure = PathMeasure()
    private val strokePartial = Path()
    private val strokePartialAdditional = Path()

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL_AND_STROKE
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFEB008D.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dp(2f, context)
    }

    private val holeWidth = dp(260f, context)
    private val holeHeight = dp(360f, context)
    private val holeStrokeOffset = dp(10f, context)

    private var lastCanvasWidth = 0
    private var lastCanvasHeight = 0
    private var lastCropRegionWidth = 0
    private var lastCropRegionHeight = 0

    private var strokeStartD = 0f

    private var inCameraView = false
    override val shouldInvalidateOnNextFrame: Boolean
        get() {
            return inCameraView
        }


    override fun draw(cropRegionWidth: Int, cropRegionHeight: Int, canvas: Canvas?) {
        if (canvas == null || cropRegionHeight == 0 || cropRegionWidth == 0 || canvas.width == 0) return


        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()

        val changed = cropRegionWidth != lastCropRegionWidth || cropRegionHeight != lastCropRegionHeight || lastCanvasWidth != canvas.width || lastCanvasHeight != canvas.height

        if (changed) {
            lastCanvasWidth = canvas.width
            lastCanvasHeight = canvas.height
            lastCropRegionWidth = cropRegionWidth
            lastCropRegionHeight = cropRegionWidth

            mask.reset()
            strokeFull.reset()
            //замкнутый путь "по всему экрану"
            mask.moveTo(0f, 0f)
            mask.lineTo(canvasWidth, 0f)
            mask.lineTo(canvasWidth, canvasHeight)
            mask.lineTo(0f, canvasHeight)
            mask.lineTo(0f, 0f)
            mask.close()

            //а это — замкнутый путь "чуть поменьше", который благодаря правилу заполнения evenOdd останется прозрачным

            val holeTop = canvasHeight * 0.1f
            val holeLeft = (canvasWidth - holeWidth) / 2
            val holeRight = holeLeft + holeWidth
            val holeBottom = holeTop + holeHeight

            val holeHorizontalMiddle = holeLeft + holeWidth / 2
            val holeVerticalMiddle = holeTop + holeHeight / 3



            mask.moveTo(holeHorizontalMiddle, holeTop)
            mask.quadTo(holeRight, holeTop, holeRight, holeVerticalMiddle)
            mask.quadTo(holeRight, holeBottom, holeHorizontalMiddle, holeBottom)
            mask.quadTo(holeLeft, holeBottom, holeLeft, holeVerticalMiddle)
            mask.quadTo(holeLeft, holeTop, holeHorizontalMiddle, holeTop)
            mask.close()


            strokeFull.moveTo(holeHorizontalMiddle, holeTop - holeStrokeOffset)
            strokeFull.quadTo(holeRight + holeStrokeOffset, holeTop - holeStrokeOffset , holeRight + holeStrokeOffset, holeVerticalMiddle)
            strokeFull.quadTo(holeRight + holeStrokeOffset, holeBottom + holeStrokeOffset, holeHorizontalMiddle, holeBottom + holeStrokeOffset)
            strokeFull.quadTo(holeLeft - holeStrokeOffset, holeBottom + holeStrokeOffset, holeLeft - holeStrokeOffset, holeVerticalMiddle)
            strokeFull.quadTo(holeLeft - holeStrokeOffset, holeTop - holeStrokeOffset, holeHorizontalMiddle, holeTop - holeStrokeOffset)
            strokeFull.close()

            strokeMeasure.setPath(strokeFull, false)
        }

        val length = strokeMeasure.length
        val needToDrawAdditional = strokeStartD > 0.75f
        strokePartial.reset()
        if (needToDrawAdditional) {
            strokePartialAdditional.reset()
            strokeMeasure.getSegment(length * strokeStartD, length, strokePartial, true)
            strokeMeasure.getSegment(0f, length * (strokeStartD - 0.75f), strokePartialAdditional, true)
        } else {
            strokeMeasure.getSegment(length * strokeStartD, length * (0.25f + strokeStartD), strokePartial, true)
        }

        strokePartial.rLineTo(0f, 0f) // workaround to display on hardware accelerated canvas as described in docs

        canvas.drawPath(mask, maskPaint)
        if (inCameraView) {
            canvas.drawPath(strokePartial, strokePaint)
            if (needToDrawAdditional) {
                canvas.drawPath(strokePartialAdditional, strokePaint)
            }
        }

        if (strokeStartD >= 1f) {
            strokeStartD = 0f
        } else {
            strokeStartD += 0.005f
        }
    }

    override fun receiveEvent(data: DataBundle) = Unit

    override fun notifyInCameraView() {
        inCameraView = true
    }

    override fun notifyPictureTaken() {
        inCameraView = false
    }

}