package com.dbrain.flow.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.dbrain.flow.common.FlowParams
import com.dbrain.flow.common.dp


internal class DocumentBordersView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = dp(4)
        strokeCap = Paint.Cap.SQUARE
    }

    private var flowParams: FlowParams = FlowParams()
    private var viewWidth = 0f
    private var viewHeight = 0f

    var isClipping = false
        set(value) {
            field = value
            invalidate()
        }

    fun setFlowParams(flowParams: FlowParams) {
        this.flowParams = flowParams
        calculateSize()
        invalidate()
    }

    private fun calculateSize() {
        viewWidth = measuredWidth * flowParams.cropFactor
        viewHeight = viewWidth * flowParams.aspectH / flowParams.aspectW
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculateSize()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        paint.color = if (isClipping) COLOR_ERROR else COLOR_OK

        val topLeftX = (measuredWidth - viewWidth) / 2f
        val topLeftY = (measuredHeight - viewHeight) / 2f

        //draw top left
        canvas.translate(topLeftX, topLeftY)
        canvas.drawLine(0f, 0f, LINE_SIZE, 0f, paint)
        canvas.drawLine(0f, 0f, 0f, LINE_SIZE, paint)

        //draw top right
        canvas.translate(viewWidth, 0f)
        canvas.drawLine(0f, 0f, -LINE_SIZE, 0f, paint)
        canvas.drawLine(0f, 0f, 0f, LINE_SIZE, paint)

        //draw bottom right
        canvas.translate(0f, viewHeight)
        canvas.drawLine(0f, 0f, -LINE_SIZE, 0f, paint)
        canvas.drawLine(0f, 0f, 0f, -LINE_SIZE, paint)

        //draw bottom left
        canvas.translate(-viewWidth, 0f)
        canvas.drawLine(0f, 0f, 0f, -LINE_SIZE, paint)
        canvas.drawLine(0f, 0f, LINE_SIZE, 0f, paint)
    }

    companion object {
        private const val COLOR_OK = 0xffffffff.toInt()
        private const val COLOR_ERROR = 0xffff0000.toInt()
        private val LINE_SIZE = dp(24)
    }

}