package com.dbrain.flowdemo.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.dbrain.flowdemo.R

class DProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var animation = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 1500
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            val value = it.animatedValue as Float
            this@DProgressBar.rotation = value
        }
    }

    init {
        setBackgroundResource(R.drawable.ic_progress_bar)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            animation.start()
        } else {
            animation.pause()
        }
    }
}
