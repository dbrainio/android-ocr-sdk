package com.dbrain.facerecognizer

import android.os.Bundle
import android.view.View
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.recognition.camera.CropParameters
import com.dbrain.recognition.processors.DataBundle

class FaceCaptureActivity : CaptureActivity() {
    private var cameraFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
    private var cropParameters: CropParameters? = null

    override fun getDrawer() = FaceDrawer(this)
    override fun getProcessor(parameters: Bundle?) = FaceProcessor(this, parameters, this)
    override fun getCameraFacing() = cameraFacing
    override fun getCropRegionParameters() = cropParameters

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.extras?.let {
            cameraFacing = it.getInt(ARG_CAMERA_FACING)
            val cropX = it.getFloat(ARG_CROP_REGION_X)
            val cropY = it.getFloat(ARG_CROP_REGION_Y)
            val cropScale = it.getFloat(ARG_CROP_SCALE)
            cropParameters = CropParameters(cropX, cropY, cropScale)
        }
        super.onCreate(savedInstanceState)
        getTitleView()?.setText(R.string.face_recognizer_title)
    }

    override fun onProcessorEvent(dataBundle: DataBundle) {
        super.onProcessorEvent(dataBundle)
        if (dataBundle is FaceDataBundle) {
            getErrorDescriptionView()?.let {
                val textResource = when (dataBundle.state) {
                    FaceDataBundle.STATE_NO_FACE_DETECTED -> R.string.no_face_detected
                    FaceDataBundle.STATE_DETECTED_NOT_FULL, FaceDataBundle.STATE_DETECTED_TURNED_RIGHT_AFTER_LEFT_AND_FULL -> R.string.turn_head_straight
                    FaceDataBundle.STATE_DETECTED_FULL_NOT_CHECKED -> R.string.turn_head_left
                    FaceDataBundle.STATE_DETECTED_TURNED_LEFT_AFTER_FULL -> R.string.turn_head_right
                    else -> null
                }
                if (textResource != null) {
                    it.setText(textResource)
                } else {
                    it.text = null
                }

                it.visibility = if (dataBundle.detected) View.INVISIBLE else View.VISIBLE
            }
        }
    }

}