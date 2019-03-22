package com.dbrain.recognition.camera

data class CropParameters(
    val ratioWidth: Float,
    val ratioHeight: Float,
//    @FloatRange(from = 0.1, to = 1.0)
    val cropScale: Float
)