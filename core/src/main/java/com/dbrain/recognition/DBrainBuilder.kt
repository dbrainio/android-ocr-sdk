package com.dbrain.recognition

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.dbrain.recognition.activities.CaptureActivity

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
abstract class DBrainBuilder(
    protected val cameraFacing: Int = CAMERA_FACING_BACK,
    protected val cropRatioWidth: Float = CROP_RATIO_UNDEFINED,
    protected val cropRatioHeight: Float = CROP_RATIO_UNDEFINED,
    protected val cropScale: Float = 1f
) {

    abstract fun getIntent(context: Context?): Intent?

    protected fun build(context: Context?): Intent? {
        val intent = getIntent(context) ?: return null
        intent.run {
            putExtra(CaptureActivity.ARG_CROP_SCALE, cropScale)
            putExtra(CaptureActivity.ARG_CROP_REGION_X, cropRatioWidth)
            putExtra(CaptureActivity.ARG_CROP_REGION_Y, cropRatioHeight)
            putExtra(CaptureActivity.ARG_CAMERA_FACING, cameraFacing)
            this
        }
        return intent
    }

    fun buildAndStartForResult(activity: Activity, requestCode: Int) {
        activity.startActivityForResult(build(activity), requestCode)
    }

    fun buildAndStartForResult(fragment: Fragment, requestCode: Int) {
        fragment.startActivityForResult(build(fragment.context), requestCode)
    }

    fun buildAndStart(activity: Activity) {
        activity.startActivity(build(activity))
    }

    fun buildAndStart(fragment: Fragment) {
        fragment.startActivity(build(fragment.context))
    }

    companion object {
        const val CAMERA_FACING_BACK = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
        const val CAMERA_FACING_FRONT = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
        const val CROP_RATIO_UNDEFINED = -1f
    }
}
