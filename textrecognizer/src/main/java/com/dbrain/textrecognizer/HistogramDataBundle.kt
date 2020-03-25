package com.dbrain.textrecognizer

import com.dbrain.recognition.processors.DataBundle

data class HistogramDataBundle(
    val histogramData: IntArray,
    val xThresholdValue: Int,
    val yThresholdValue: Int
) : DataBundle(true)