package com.dbrain.recognition.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import com.dbrain.recognition.utils.getAppTypeface

class AppTypefaceButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    Button(context, attrs, defStyleAttr) {
    init {
        typeface = getAppTypeface(context)
    }
}