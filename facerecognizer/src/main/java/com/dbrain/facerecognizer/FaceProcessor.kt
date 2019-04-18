package com.dbrain.facerecognizer

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import com.dbrain.recognition.DBrainBuilder.Companion.CAMERA_FACING_BACK
import com.dbrain.recognition.activities.CaptureActivity.Companion.ARG_CAMERA_FACING
import com.dbrain.recognition.processors.Processor
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlin.math.abs
import kotlin.math.sign


class FaceProcessor(
    context: Context,
    parameters: Bundle?,
    listener: Listener
) : Processor(context, listener) {

    companion object {

        const val EULER_ANGLE_Y_STRAIGHT_ABS_THRESHOLD = 10
        const val EULER_ANGLE_Y_TURNED_ABS_THRESHOLD = 30
        const val EULER_ANGLE_Y_TURN_LEFT_SIGN = 1
        const val EULER_ANGLEY_Y_TURN_RIGHT_SIGN = -1
    }

    private val lensFacing = parameters?.getInt(ARG_CAMERA_FACING) ?: 0
    private val dataBundle = FaceDataBundle()
    private val rotation = if (lensFacing == CAMERA_FACING_BACK) FirebaseVisionImageMetadata.ROTATION_90 else FirebaseVisionImageMetadata.ROTATION_270

    //TODO настройки через FirebaseVisionFaceDetectorOptions.Builder
    private val detector by lazy { FirebaseVision.getInstance().visionFaceDetector }
    @Volatile private var detectorIsRunning = false

    override fun close() {
        detector.close()
    }

    override fun processFrame(
        croppedBitmap: Bitmap?,
        originalByteArray: ByteArray?,
        cameraPreviewWidth: Int,
        cameraPreviewHeight: Int,
        frameIndex: Int
    ) {
//        Log.d("FaceProcessor", "processFrame called, detectorIsRunning = $detectorIsRunning")
        if (originalByteArray == null || detectorIsRunning) return
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(cameraPreviewWidth)
            .setHeight(cameraPreviewHeight)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(rotation)
            .build()

        val image = FirebaseVisionImage.fromByteArray(originalByteArray, metadata)
        detectorIsRunning = true
        detector.detectInImage(image)
            .addOnSuccessListener { foundFaces ->
                dataBundle.apply {
                    val firstFace = foundFaces.firstOrNull()
                    val currentState = state
                    val nextState =
                        if (firstFace == null) FaceDataBundle.STATE_NO_FACE_DETECTED
                        else {
                            val angleY = firstFace.headEulerAngleY
//                            Log.d("FaceProcessor", "euler angle Y: $angleY")
                            val fullFace = abs(angleY) < EULER_ANGLE_Y_STRAIGHT_ABS_THRESHOLD                                   //анфас
                            val turned = !fullFace && abs(angleY) > EULER_ANGLE_Y_TURNED_ABS_THRESHOLD                          //поворот
                            val turnedLeft = turned && sign(angleY).toInt() == EULER_ANGLE_Y_TURN_LEFT_SIGN                     //поворот и куда именно
                            val turnedRight = !turnedLeft && turned && sign(angleY).toInt() == EULER_ANGLEY_Y_TURN_RIGHT_SIGN

                            when (currentState) {
                                FaceDataBundle.STATE_NO_FACE_DETECTED, FaceDataBundle.STATE_DETECTED_NOT_FULL -> if (fullFace) FaceDataBundle.STATE_DETECTED_FULL_NOT_CHECKED else FaceDataBundle.STATE_DETECTED_NOT_FULL
                                FaceDataBundle.STATE_DETECTED_FULL_NOT_CHECKED -> if (turnedLeft) FaceDataBundle.STATE_DETECTED_TURNED_LEFT_AFTER_FULL else FaceDataBundle.STATE_DETECTED_FULL_NOT_CHECKED
                                FaceDataBundle.STATE_DETECTED_TURNED_LEFT_AFTER_FULL -> if (turnedRight) FaceDataBundle.STATE_DETECTED_TURNED_RIGHT_AFTER_LEFT_AND_FULL else FaceDataBundle.STATE_DETECTED_TURNED_LEFT_AFTER_FULL
                                FaceDataBundle.STATE_DETECTED_TURNED_RIGHT_AFTER_LEFT_AND_FULL -> if (fullFace) FaceDataBundle.STATE_DETECTED_FULLY_CHECKED else FaceDataBundle.STATE_DETECTED_TURNED_RIGHT_AFTER_LEFT_AND_FULL
                                FaceDataBundle.STATE_DETECTED_FULLY_CHECKED -> FaceDataBundle.STATE_DETECTED_FULLY_CHECKED
                                else -> FaceDataBundle.STATE_NO_FACE_DETECTED       //actually, should never happen
                            }
                        }
                    state = nextState
                    face = firstFace
                    postEvent(dataBundle)
                    detectorIsRunning = false
                }
            }
            .addOnFailureListener(
                object : OnFailureListener {

                    override fun onFailure(e: Exception) {
                        dataBundle.apply {
                            state = FaceDataBundle.STATE_NO_FACE_DETECTED
                            face = null
                        }
                        postEvent(dataBundle)
                        detectorIsRunning = false
                    }
                })

    }

    override fun notifyPictureTaken() {
        dataBundle.apply {
            state = FaceDataBundle.STATE_NO_FACE_DETECTED
            face = null
        }
        postEvent(dataBundle)
    }

    init {
        FirebaseApp.initializeApp(context)
    }
}
