package com.dbrain.recognition.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import com.dbrain.recognition.R
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.getAppTypeface

class RoundedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    Button(context, attrs, defStyleAttr) {
    init {
        setBackgroundResource(R.drawable.button_selector)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        setTextColor(0xffffffff.toInt())
        val padding = dp(30f, context).toInt()
        setPadding(padding, 0, padding, 0)
        gravity = Gravity.CENTER
        typeface = getAppTypeface(context)
    }
}