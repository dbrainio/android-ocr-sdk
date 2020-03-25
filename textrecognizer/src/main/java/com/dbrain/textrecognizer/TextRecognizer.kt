package com.dbrain.textrecognizer

import android.content.Context
import android.content.Intent
import com.dbrain.recognition.DBrainBuilder

object TextRecognizer {
    /**
     * Base builder class for launching capture activity.
     * @param cameraFacing facing of camera lens. Can be {@link #CAMERA_FACING_BACK} or {@link #CAMERA_FACING_FRONT}
     *
     * Crop parameters for image processing. By default - whole image will be processed.
     * @param cropRatioWidth dividend value of the aspect ratio of the crop region.
     * @param cropRatioHeight divider value of the aspect ratio of the crop region.
     * For example, use 16/10 to crop the rectangle from the camera image.
     * Use {@link #CROP_RATIO_UNDEFINED} for both if you don't want to crop picture.
     * @param cropScale scale factor of the crop region.
     * Cropping is always done at the center of the image.
     */
    class Builder(
        cameraFacing: Int = CAMERA_FACING_BACK,
        cropRatioWidth: Float = CROP_RATIO_UNDEFINED,
        cropRatioHeight: Float = CROP_RATIO_UNDEFINED,
        cropScale: Float = 1f
    ) : DBrainBuilder(
        cameraFacing, cropRatioWidth, cropRatioHeight, cropScale
    ) {
        override fun getIntent(context: Context?) = Intent(context, TextCaptureActivity::class.java)

    }

    const val CAMERA_FACING_BACK = DBrainBuilder.CAMERA_FACING_BACK
    const val CAMERA_FACING_FRONT = DBrainBuilder.CAMERA_FACING_FRONT
}