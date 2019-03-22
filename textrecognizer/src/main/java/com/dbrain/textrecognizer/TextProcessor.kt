package com.dbrain.textrecognizer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import com.dbrain.recognition.DBrainBuilder
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Processor
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.nio.ByteBuffer

class TextProcessor(
    context: Context,
    parameters: Bundle?,
    listener: Listener
) : Processor(context, listener),
    Detector.Processor<TextBlock> {
    private var textRecognizer: TextRecognizer? = null
    private val MIN_ALLOWED_TEXT_LENGTH = 20
    private var detected = true
    private val frameBuilder = Frame.Builder()
    private val rotation =
        if (parameters?.getInt(CaptureActivity.ARG_CROP_SCALE) == DBrainBuilder.CAMERA_FACING_BACK) 1 else 3
    private val dataBundle = DataBundle()

    override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
        val items = detections.detectedItems
        var textLength = 0
        for (i in 0 until items.size()) {
            val item = items.valueAt(i)
            textLength += item.value.length
            if (textLength >= MIN_ALLOWED_TEXT_LENGTH) break
        }

        val detected = textLength >= MIN_ALLOWED_TEXT_LENGTH
        if (this.detected != detected) {
            this.detected = detected
            dataBundle.detected = detected
            postEvent(dataBundle)
        }
    }

    override fun release() {

    }

    override fun close() {
        textRecognizer?.release()
    }

    override fun processFrame(
        croppedBitmap: Bitmap?,
        originalByteArray: ByteArray?,
        cameraPreviewWidth: Int,
        cameraPreviewHeight: Int,
        frameIndex: Int
    ) {
        if (textRecognizer == null) textRecognizer = TextRecognizer.Builder(context).build().apply {
            setProcessor(this@TextProcessor)
        }

        val frame = frameBuilder
            .setId(frameIndex)
            .setTimestampMillis(System.currentTimeMillis())
            .setRotation(rotation)

        if (croppedBitmap != null)
            frame.setBitmap(croppedBitmap)
        else
            frame.setImageData(
                ByteBuffer.wrap(originalByteArray, 0, originalByteArray!!.size),
                cameraPreviewWidth,
                cameraPreviewHeight,
                ImageFormat.NV21
            )

        textRecognizer?.receiveFrame(frame.build())
    }

}
