package com.dbrain.facerecognizer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import com.dbrain.recognition.DBrainBuilder.Companion.CAMERA_FACING_BACK
import com.dbrain.recognition.activities.CaptureActivity.Companion.ARG_CAMERA_FACING
import com.dbrain.recognition.processors.Processor
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.FaceDetector
import java.nio.ByteBuffer


class FaceProcessor(
    context: Context,
    parameters: Bundle?,
    listener: Listener
) : Processor(context, listener) {

    private var detector: FaceDetector? = null
    private val lensFacing = parameters?.getInt(ARG_CAMERA_FACING) ?: 0
    private val facedetectorBuilder = FaceDetector.Builder(context)
        .setTrackingEnabled(false)
        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
        .setProminentFaceOnly(parameters?.getBoolean(FaceRecognizer.ARG_PROMINENT_FACE_ONLY) ?: true)
    private val dataBundle = FaceDataBundle()
    private val frameBuilder = Frame.Builder()
    private val rotation = if (lensFacing == CAMERA_FACING_BACK) 1 else 3

    override fun close() {
        detector?.release()
    }

    override fun processFrame(
        croppedBitmap: Bitmap?,
        originalByteArray: ByteArray?,
        cameraPreviewWidth: Int,
        cameraPreviewHeight: Int,
        frameIndex: Int
    ) {
        if (originalByteArray == null) return
        if (detector == null) detector = facedetectorBuilder.build()

        val frame = frameBuilder
            .setId(frameIndex)
            .setTimestampMillis(System.currentTimeMillis())
            .setRotation(rotation)

        if (croppedBitmap != null)
            frame.setBitmap(croppedBitmap)
        else
            frame.setImageData(
                ByteBuffer.wrap(originalByteArray, 0, originalByteArray.size),
                cameraPreviewWidth,
                cameraPreviewHeight,
                ImageFormat.NV21
            )

        val faces = detector?.detect(frame.build()) ?: return

        dataBundle.apply {
            detected = faces.size() > 0
            this.faces = faces
            this.previewWidth = cameraPreviewWidth
            this.previewHeight = cameraPreviewHeight
            this.cameraFacing = lensFacing
        }
        postEvent(dataBundle)
    }
}
