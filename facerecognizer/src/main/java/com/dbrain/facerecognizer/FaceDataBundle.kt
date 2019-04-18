package com.dbrain.facerecognizer

import com.dbrain.recognition.processors.DataBundle
import com.google.firebase.ml.vision.face.FirebaseVisionFace

class FaceDataBundle(
    var state: Int = STATE_NO_FACE_DETECTED,
    var face: FirebaseVisionFace? = null
) : DataBundle() {

    companion object {

        const val STATE_NO_FACE_DETECTED = 0                                                    //не распознали лицо
        const val STATE_DETECTED_NOT_FULL = 1                                                   //распознали, но не в анфас
        const val STATE_DETECTED_FULL_NOT_CHECKED = 2                                           //распознали в анфас, но не проверили
        const val STATE_DETECTED_TURNED_LEFT_AFTER_FULL = 3                                     //распознали поворот влево после анфаса
        const val STATE_DETECTED_TURNED_RIGHT_AFTER_LEFT_AND_FULL = 4                           //распознали поворот вправо после анфаса и поворота влево
        const val STATE_DETECTED_FULLY_CHECKED = 5                                              //распознали анфас после процедуры liveness check
    }

    override var detected: Boolean
        get() {
            return state == STATE_DETECTED_FULLY_CHECKED
        }
        set(value) = Unit


}