package com.dbrain.textrecognizer

import android.os.Bundle
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.recognition.camera.CropParameters

class TextCaptureActivity : CaptureActivity() {
    private var cameraFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
    private var cropParameters: CropParameters? = null

    override fun getDrawer() = TextDrawer(this)
    override fun getProcessor(parameters: Bundle?) = TextProcessor(this, parameters, this)
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
        getTitleView()?.setText(R.string.title)
    }



}