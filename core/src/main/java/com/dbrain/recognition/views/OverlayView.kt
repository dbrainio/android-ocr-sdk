package com.dbrain.recognition.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.dbrain.recognition.processors.Drawer

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    protected var regionWidth = 0
        private set(v) {
            field = v
        }
    protected var regionHeight = 0
        private set(v) {
            field = v
        }
    var drawer: Drawer? = null
        set(v) {
            field = v
            invalidate()
        }

    fun setRegion(width: Int, height: Int) {
        this.regionWidth = width
        this.regionHeight = height
        requestLayout()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawer?.draw(regionWidth, regionHeight, canvas)
        if (drawer?.shouldInvalidateOnNextFrame == true) {
            postInvalidateOnAnimation()
        }
    }
}