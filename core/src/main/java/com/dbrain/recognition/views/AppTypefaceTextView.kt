package com.dbrain.recognition.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.dbrain.recognition.utils.getAppTypeface

class AppTypefaceTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    TextView(context, attrs, defStyleAttr) {
    init {
        typeface = getAppTypeface(context)
    }
}