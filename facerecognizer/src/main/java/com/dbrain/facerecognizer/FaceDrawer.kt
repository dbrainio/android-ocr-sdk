package com.dbrain.facerecognizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.SparseArray
import androidx.core.content.ContextCompat
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Drawer
import com.dbrain.recognition.utils.dp
import com.google.android.gms.vision.face.Face


class FaceDrawer(context: Context) : Drawer() {
    private var circleRadius = dp(4f, context)
    private val colorRed = ContextCompat.getColor(context, R.color.app_red)
    private val colorGreen = ContextCompat.getColor(context, R.color.app_green)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(2f, context)
        style = Paint.Style.STROKE
        color = colorGreen
    }

    private var dash = dp(16f, context)
    private var dashPath = DashPathEffect(floatArrayOf(dash, dash), 1.0f)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(4f, context)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        pathEffect = dashPath
        color = colorRed
    }
    private val rect = RectF()

    private var faces: SparseArray<Face>? = null
    private var detected = false
    private var previewWidth = 1
    private var previewHeight = 1
    private var lensFacing = 0

    override fun draw(cropRegionWidth: Int, cropRegionHeight: Int, canvas: Canvas?) {
        if (canvas == null || cropRegionHeight == 0 || cropRegionWidth == 0 || canvas.width == 0) return

        val canvasW = canvas.width
        val canvasH = canvas.height
        val regionWidth = canvasW / 1.6f
        val regionHeight = canvasH / 2f
        val left = canvasW / 2f - regionWidth / 2f
        val right = left + regionWidth
        val top = canvasH / 2f - regionHeight / 2f
        val bottom = top + regionHeight

        rect.set(left, top, right, bottom)
        circlePaint.color = if (detected) colorGreen else colorRed
        canvas.drawOval(rect, circlePaint)

        val xScale = previewHeight / canvas.width.toFloat()
        val yScale = previewWidth / canvas.height.toFloat()
        faces?.let {
            for (i in 0 until it.size()) {
                val face = it.valueAt(i)
                for (landmark in face.landmarks) {
                    val x = if (lensFacing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
                        canvas.width - (landmark.position.x / xScale)
                    else
                        landmark.position.x / xScale
                    val y = landmark.position.y / yScale
                    if (rect.contains(x, y)) {
                        canvas.drawCircle(
                            x,
                            y,
                            circleRadius, paint
                        )
                    }
                }
            }
        }
    }

    override fun receiveEvent(data: DataBundle) {
        if (data is FaceDataBundle) {
            detected = data.detected
            faces = data.faces
            this.previewWidth = data.previewWidth
            this.previewHeight = data.previewHeight
            this.lensFacing = data.cameraFacing
        }
    }

}