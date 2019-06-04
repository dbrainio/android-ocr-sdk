package com.dbrain.thickness

import android.content.Context
import android.content.Intent
import com.dbrain.recognition.DBrainBuilder

object ThicknessDetector {
    /**
     * Base builder class for launching thickness detection activity.
     * @param cameraFacing facing of camera lens. Can be {@link #CAMERA_FACING_BACK} or {@link #CAMERA_FACING_FRONT}
     * @param prominentFaceOnly Indicates whether to detect all faces, or to only detect the most prominent face
     * (i.e., a large face that is most central within the frame)
     */
    class Builder(
        cameraFacing: Int = CAMERA_FACING_BACK
    ) : DBrainBuilder(cameraFacing) {
        override fun getIntent(context: Context?): Intent {
            return Intent(context, ThicknessDetectionCaptureActivity::class.java)
        }

    }

}
