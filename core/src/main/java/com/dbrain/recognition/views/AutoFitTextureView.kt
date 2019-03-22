package com.dbrain.recognition.views

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.View

class AutoFitTextureView : TextureView {

    private var ratioWidth = 0
    private var ratioHeight = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    fun setAspectRatio(width: Int, height: Int) {
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width > height) {
                val newHeight = width * ratioHeight / ratioWidth
                setMeasuredDimension(
                    width,
                    newHeight
                )
                translationY = -(newHeight - height) / 2f
            } else {
                val newWidth = height * ratioWidth / ratioHeight
                setMeasuredDimension(
                    newWidth,
                    height
                )
                translationX = -(newWidth - width) / 2f
            }
        }
    }
}