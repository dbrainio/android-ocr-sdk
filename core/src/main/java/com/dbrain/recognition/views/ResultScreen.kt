package com.dbrain.recognition.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dbrain.recognition.R
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.getAppTypeface

class ResultScreen(
    context: Context,
    private val result: Int,
    private val listener: Listener?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    interface Listener {
        fun onResultScreenButtonClicked(result: Int)
    }

    init {
        isClickable = true

        setBackgroundColor(
            if (result == RESULT_ERROR) ContextCompat.getColor(context, R.color.app_red)
            else 0xff000000.toInt()
        )

        val button = RoundedButton(context).apply {
            text = if (result == RESULT_OK) context.getString(R.string.done)
            else context.getString(R.string.retake)
            setBackgroundResource(R.drawable.button_white_selector)
            setTextColor(0xff000000.toInt())
            setOnClickListener {
                listener?.onResultScreenButtonClicked(result)
            }
        }

        addView(
            button, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                dp(48f, context).toInt(),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = dp(48f, context).toInt()
            }
        )


        val text = TextView(context).apply {
            setTextColor(0xffffffff.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
            text = if (result == RESULT_OK) context.getString(R.string.all_done)
            else context.getString(R.string.sending_failed)
            gravity = Gravity.CENTER
            typeface = getAppTypeface(context)
        }

        addView(
            text, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ).apply {
                bottomMargin = dp(72f, context).toInt()
            }
        )
    }


    companion object {
        const val RESULT_OK = 1
        const val RESULT_ERROR = 2
    }
}