package com.dbrain.facerecognizer

import android.content.Context
import android.content.Intent
import com.dbrain.recognition.DBrainBuilder

object FaceRecognizer {
    /**
     * Base builder class for launching capture activity.
     * @param cameraFacing facing of camera lens. Can be {@link #CAMERA_FACING_BACK} or {@link #CAMERA_FACING_FRONT}
     * @param prominentFaceOnly Indicates whether to detect all faces, or to only detect the most prominent face
     * (i.e., a large face that is most central within the frame)
     */
    class Builder(
        cameraFacing: Int = CAMERA_FACING_BACK,
        val prominentFaceOnly: Boolean = true
    ) : DBrainBuilder(cameraFacing) {
        override fun getIntent(context: Context?): Intent {
            return Intent(context, FaceCaptureActivity::class.java).run {
                putExtra(ARG_PROMINENT_FACE_ONLY, prominentFaceOnly)
                this
            }
        }

    }

    const val CAMERA_FACING_BACK = DBrainBuilder.CAMERA_FACING_BACK
    const val CAMERA_FACING_FRONT = DBrainBuilder.CAMERA_FACING_FRONT

    const val ARG_PROMINENT_FACE_ONLY = "PROMINENT_ONLY"

}
