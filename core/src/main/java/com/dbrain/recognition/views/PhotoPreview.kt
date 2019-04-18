package com.dbrain.recognition.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.dbrain.recognition.R
import com.dbrain.recognition.processors.Drawer
import com.dbrain.recognition.utils.dp

class PhotoPreview(
    context: Context,
    val byteArray: ByteArray,
    val listener: Listener?,
    val facing: Int,
    val overlayDrawer: Drawer?,
    val cropWidth: Int,
    val cropHeight: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    interface Listener {
        fun onPhotoPreviewBackPressed()
        fun onPhotoPreviewSendPressed(byteArray: ByteArray)
    }

    private val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    private val overlayView = OverlayView(context).apply {
        drawer = overlayDrawer
        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        setRegion(cropWidth, cropHeight)
    }

    private val titleView = AppTypefaceTextView(context).apply {
        setText(R.string.confirmation_title)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        setTextColor(Color.BLACK)
        setPadding(dp(32f, context).toInt(), 0, dp(32f, context).toInt(), 0)
        gravity = Gravity.CENTER_HORIZONTAL
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(4f, context).toInt()
        lp.bottomMargin = dp(4f, context).toInt()
        layoutParams = lp
    }

    private val retakeButton = AppTypefaceButton(context).apply {
        setText(R.string.confirmation_retake)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        setTextColor(0xFF0075EB.toInt())
        setPadding(dp(32f, context).toInt(), dp(16f, context).toInt(), dp(32f, context).toInt(), dp(16f, context).toInt())
        gravity = Gravity.CENTER_HORIZONTAL
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(4f, context).toInt()
        lp.bottomMargin = dp(4f, context).toInt()
        layoutParams = lp

        setOnClickListener {
            listener?.onPhotoPreviewBackPressed()
        }
    }



    private val sendButton = AppTypefaceButton(context).apply {
        setText(R.string.confirmation_submit)
        setBackgroundResource(R.drawable.round_button_selector)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        setTextColor(Color.WHITE)
        setPadding(dp(32f, context).toInt(), 0, dp(32f, context).toInt(), 0)
        gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(4f, context).toInt()
        lp.bottomMargin = dp(4f, context).toInt()

        setOnClickListener {
            listener?.onPhotoPreviewSendPressed(byteArray)
            showSending()
        }
    }

    private val titleAndRetakeContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(titleView)
        addView(retakeButton)
    }


    init {
        isClickable = true
        addView(imageView)
        addView(overlayView)
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
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
            titleAndRetakeContainer, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                bottomMargin = dp(80f, context).toInt()
            }
        )

        addView(
            sendButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                dp(48f, context).toInt(),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                leftMargin = dp(32f, context).toInt()
                rightMargin = dp(32f, context).toInt()
                bottomMargin = dp(16f, context).toInt()
            }
        )
    }

    private fun showSending() {
        sendButton.isEnabled = false
        sendButton.setText(R.string.loading)
    }
}