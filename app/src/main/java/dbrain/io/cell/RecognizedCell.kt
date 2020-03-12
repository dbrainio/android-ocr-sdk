package dbrain.io.cell

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import com.dbrain.recognition.data.RecognizedField
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.normalizeType
import dbrain.io.R

class RecognizedCell(context: Context): LinearLayout(context) {

    private val fieldNameView = TextView(context)
    private val accuracyView = TextView(context)
    private val textView = TextView(context)

    init {
        orientation = HORIZONTAL
        addView(fieldNameView, getLayoutParamsForText(1f))
        val container = FrameLayout(context)
        container.addView(accuracyView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        })
        addView(container, getLayoutParamsForText(1.2f))
        addView(textView, getLayoutParamsForText(.9f))
        initTextViews()
    }

    fun bind(field: RecognizedField) {
        fieldNameView.text = normalizeType(field.fieldName)
        textView.text = field.text
        accuracyView.setBackgroundResource(getBackgroundForAccuracy(field.getAccuracy()))
        accuracyView.text = field.getAccuracy().toString()
    }

    private fun getBackgroundForAccuracy(accuracy: RecognizedField.Accuracy): Int {
        return when(accuracy) {
            RecognizedField.Accuracy.HIGH -> R.drawable.high_bg
            RecognizedField.Accuracy.MEDIUM -> R.drawable.medium_bg
            else -> R.drawable.low_bg
        }
    }

    private fun getLayoutParamsForText(weight: Float): LayoutParams {
        val margin = dp(16f, context).toInt()
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight)
        layoutParams.gravity = Gravity.CENTER
        layoutParams.setMargins(margin, margin, margin, margin)
        return layoutParams
    }

    private fun initTextViews() {
        fieldNameView.apply {
            gravity = Gravity.CENTER_VERTICAL
            textSize = 14f
            setTextColor(Color.BLACK)
        }
        textView.apply {
            gravity = Gravity.CENTER_VERTICAL
            textSize = 14f
            setTextColor(Color.BLACK)
        }
        accuracyView.apply {
            val padding = dp(6f, context).toInt()
            setPadding(padding * 2, padding, padding * 2, padding)
            accuracyView.textSize = 12f
            accuracyView.setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
    }
}