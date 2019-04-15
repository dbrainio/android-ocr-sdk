package com.dbrain.facerecognizer

import android.util.SparseArray
import com.dbrain.recognition.processors.DataBundle
import com.google.android.gms.vision.face.Face

class FaceDataBundle(
    detected: Boolean = false,
    var faces: SparseArray<Face>? = null,
    var previewWidth: Int = 0,
    var previewHeight: Int = 0,
    var cameraFacing: Int = 0
) : DataBundle(detected)