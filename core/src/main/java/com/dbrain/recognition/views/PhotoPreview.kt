package com.dbrain.recognition.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.dbrain.recognition.R
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.getAppTypeface

class PhotoPreview(
    context: Context,
    val fileName: String,
    val listener: Listener?,
    val facing: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    interface Listener {
        fun onPhotoPreviewBackPressed()
        fun onPhotoPreviewSendPressed(fileName: String)
    }

    private val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    private val sendButton = RoundedButton(context).apply {
        text = context.getString(R.string.send)
        setOnClickListener {
            listener?.onPhotoPreviewSendPressed(fileName)
            showSending()
        }
    }
    private val backButton = ImageButton(context).apply {
        setBackgroundResource(R.drawable.button_selector)
        setImageResource(R.drawable.ic_close_white_24dp)
        fitsSystemWindows = true
        setOnClickListener {
            listener?.onPhotoPreviewBackPressed()
        }
    }

    private val sendingText = TextView(context).apply {
        typeface = getAppTypeface(context)
        text = context.getString(R.string.sending)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        gravity = Gravity.CENTER
        setTextColor(0xffffffff.toInt())
        setBackgroundResource(R.drawable.big_black_rounded_rectangle)
    }


    init {
        isClickable = true
        addView(imageView)
        val bitmap = BitmapFactory.decodeFile(fileName)
        val flip = facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
        val m = Matrix()
        m.invert(m)
        m.setRotate(if (flip) 270f else 90f)
        if (flip)
            m.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        val finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        bitmap.recycle()
        imageView.setImageBitmap(finalBitmap)

        addView(
            sendButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                dp(48f, context).toInt(),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = dp(48f, context).toInt()
            }
        )
        addView(
            backButton, FrameLayout.LayoutParams(
                dp(48f, context).toInt(),
                dp(48f, context).toInt()
            ).apply {
                topMargin = dp(16f, context).toInt()
                leftMargin = dp(16f, context).toInt()
            }
        )
    }

    private fun showSending() {
        removeView(sendButton)
        addView(sendingText, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dp(120f, context).toInt(),
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            bottomMargin = dp(16f, context).toInt()
            leftMargin = dp(24f, context).toInt()
            rightMargin = dp(24f, context).toInt()
        })
    }
}