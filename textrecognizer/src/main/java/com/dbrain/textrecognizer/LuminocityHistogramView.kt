package com.dbrain.textrecognizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.dbrain.recognition.utils.dp

class LuminocityHistogramView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val histogram = IntArray(256)
    private val peakValue = 100000              //вообще, можно и не хардкодить, просто эта вьюшка чисто для тестовых целей

    private val bezierAlpha = 1f/3f

    private val fullPath = Path()

    private val twoDp = dp(2f, context)

    private val histogramFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFAAAAAA.toInt()
    }

    private val histogramStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = twoDp
        style = Paint.Style.STROKE
        color = 0xFFFFFFFF.toInt()
    }

    private val xBoundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = twoDp
        style = Paint.Style.STROKE
        color = Color.BLUE
    }

    private val yBoundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = twoDp
        style = Paint.Style.STROKE
        color = Color.RED
    }

    private var xThreshold: Int = 0
    private var yThreshold: Int = 0

    fun notifyHistogramDataChanged(data: IntArray, xThreshold: Int, yThreshold: Int) {
        for (i in data.indices) {
            histogram[i] = data[i]
        }
        this.xThreshold = xThreshold
        this.yThreshold = yThreshold
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) {
            return
        }

        val floatWidth = width.toFloat()
        val floatHeight = height.toFloat()

        fullPath.reset()
        //Этот moveTo нам нужен для того, чтобы впоследствии замкнуть кривую
        fullPath.moveTo(0f, floatHeight)

        //Кусок с расчётом кривой гистограммы — адаптация красивой айосной гистограммы: https://github.com/FlexMonkey/Filterpedia/blob/master/Filterpedia/components/HistogramDisplay.swift
        val firstPointY = (1.0f - (histogram[0].toFloat() / peakValue)) * floatHeight
        fullPath.moveTo(0f, firstPointY)

        val step = floatWidth / histogram.size

        val lastIndex = histogram.size - 1

        for (i in 0 until lastIndex) {


            var currentPointX = step * i
            var currentPointY = (1.0f - (histogram[i].toFloat() / peakValue)) * floatHeight
            var nextIndex = (i + 1) % histogram.size
            var prevIndex = if (i == 0) (histogram.size - 1) else (i - 1)

            var previousPointX = step * prevIndex
            var previousPointY = (1.0f - (histogram[prevIndex].toFloat() / peakValue)) * floatHeight
            var nextPointX = step * nextIndex
            var nextPointY = (1.0f - (histogram[nextIndex].toFloat() / peakValue)) * floatHeight

            val endPointX = nextPointX
            val endPointY = nextPointY

            var mx: Float
            var my: Float

            if (i > 0)
            {
                mx = (nextPointX - previousPointX) / 2f
                my = (nextPointY - previousPointY) / 2f
            } else
            {
                mx = (nextPointX - currentPointX) / 2f
                my = (nextPointY - currentPointY) / 2f
            }

            val controlPoint1X = currentPointX + mx * bezierAlpha
            val controlPoint1Y = currentPointY + my * bezierAlpha

            currentPointX = step * nextIndex
            currentPointY = (1.0f - (histogram[nextIndex].toFloat() / peakValue)) * floatHeight


            nextIndex = (nextIndex + 1) % histogram.size
            prevIndex = i

            previousPointX = step * prevIndex
            previousPointY = (1.0f - (histogram[prevIndex].toFloat() / peakValue)) * floatHeight

            nextPointX = step * nextIndex
            nextPointY = (1.0f - (histogram[nextIndex].toFloat() / peakValue)) * floatHeight

            if (i < lastIndex - 1)
            {
                mx = (nextPointX - previousPointX) / 2f
                my = (nextPointY - previousPointY) / 2f
            }
            else
            {
                mx = (currentPointX - previousPointX) / 2f
                my = (currentPointY - previousPointY) / 2f
            }

            val controlPoint2X = currentPointX - mx * bezierAlpha
            val controlPoint2Y = currentPointY - my * bezierAlpha

            fullPath.cubicTo(controlPoint1X, controlPoint1Y, controlPoint2X, controlPoint2Y, endPointX, endPointY)
        }

        //доводим линию до правого края
        fullPath.lineTo(floatWidth, floatHeight)
        //и замыкаем там, где начали
        fullPath.lineTo(0f, floatHeight)
        fullPath.close()

        canvas.drawPath(fullPath, histogramFillPaint)
        canvas.drawPath(fullPath, histogramStrokePaint)

        if (xThreshold != 0) {
            val xBoundX = (xThreshold.toFloat() / histogram.size) * floatWidth
            canvas.drawLine(xBoundX, 0f, xBoundX, floatHeight, xBoundPaint)
        }

        if (yThreshold != 0) {
            val yBoundY = (1.0f - (yThreshold.toFloat() / peakValue)) * floatHeight
            canvas.drawLine(0f, yBoundY, floatWidth, yBoundY, yBoundPaint)
        }
    }
}