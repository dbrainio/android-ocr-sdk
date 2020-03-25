package dbrain.io.cell

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.dbrain.recognition.utils.activity
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.getColorCompat
import dbrain.io.R

class BackButtonCell(context: Context): FrameLayout(context) {

    private val buttonPaddingTopBottom = dp(16f, context).toInt()
    private val buttonPaddingLeftRight = dp(20f, context).toInt()

    init {
        setPadding(0, buttonPaddingTopBottom, 0, buttonPaddingTopBottom)
        val backButtonView = TextView(context)
        backButtonView.setOnClickListener {
            context.activity()?.finish()
        }
        backButtonView.setTypeface(null, Typeface.BOLD)
        backButtonView.textSize = 16f
        backButtonView.setPadding(buttonPaddingLeftRight, buttonPaddingTopBottom, buttonPaddingLeftRight, buttonPaddingTopBottom)
        backButtonView.setText(R.string.back_to_main)
        backButtonView.setTextColor(context.getColorCompat(android.R.color.white))
        backButtonView.setBackgroundResource(R.drawable.black_button_background)
        addView(backButtonView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }
}