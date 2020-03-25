package dbrain.io.cell

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.getColorCompat
import dbrain.io.R

class RecognizedTitleCell(context: Context): LinearLayout(context) {

    private val fieldView = createTextView(context, R.string.field)
    private val confidenceView = createTextView(context, R.string.confidence)
    private val valueView = createTextView(context, R.string.value)

    init {
        orientation = HORIZONTAL
        addView(fieldView, getLayoutParamsForText(1f))
        addView(confidenceView, getLayoutParamsForText(1.2f))
        addView(valueView, getLayoutParamsForText(.9f))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.EXACTLY
            ), MeasureSpec.makeMeasureSpec(dp(56f, context).toInt(), MeasureSpec.EXACTLY)
        )
    }

    private fun getLayoutParamsForText(weight: Float): LayoutParams {
        val margin = dp(16f, context).toInt()
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight)
        layoutParams.gravity = Gravity.CENTER
        layoutParams.setMargins(margin, margin, margin, margin)
        return layoutParams
    }

    private fun createTextView(context: Context, @StringRes resId: Int): TextView {
        return TextView(context).apply {
            setText(resId)
            gravity = Gravity.CENTER_VERTICAL
            setTypeface(null, Typeface.BOLD)
            setTextColor(context.getColorCompat(android.R.color.black))
        }
    }
}