package com.dbrain.flow.common

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import kotlin.math.roundToInt

typealias ClippingListener = (isClipping: Boolean) -> Unit

internal class ClippingAnalyzer(private val listener: ClippingListener? = null) : ImageAnalysis.Analyzer {
    private var lastDecodeTime = 0L
    private val histogram = IntArray(256) { 0 }
    private val normalizedHistogram = IntArray(256) { 0 }

    private var params: FlowParams = FlowParams()

    fun setFlowParams(params: FlowParams) {
        this.params = params
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    override fun analyze(image: ImageProxy) {
        if (listener == null) {
            image.close()
            return
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDecodeTime > RECOGNIZE_ONCE_PER_MS) {
            val sideA = (image.height * params.cropFactor).toInt()
            val sideB = (sideA * params.aspectH / params.aspectW).toInt()

            val clipTop = (image.height - sideA) / 2
            val clipBottom = clipTop + sideA
            val clipLeft = (image.width - sideB) / 2
            val clipRight = clipLeft + sideB

            histogram.fill(0)
            normalizedHistogram.fill(0)

            val buffer = image.planes[0].buffer // y-plane of yuv
            val data = buffer.toByteArray()

            // measure histogram only inside crop area
            var x = 0
            var y = 0
            data.forEach { value ->
                if (x >= image.width) {
                    x = 0
                    y++
                }
                if (x in clipLeft until clipRight && y in clipTop until clipBottom) {
                    val l = value.toInt() and 0xFF
                    histogram[l]++
                }
                x++
            }

            var max = 0
            var min = Integer.MAX_VALUE
            histogram.forEach {
                if (it > max) max = it
                if (it < min) min = it
            }

            val newMax = 255f
            val newMin = 0f
            histogram.forEachIndexed { i, v ->
                normalizedHistogram[i] = ((v - min) * (newMax - newMin) / (max - min) + newMin).roundToInt()
            }

            val clippingValue = normalizedHistogram.takeLast(20).maxOrNull() ?: 0
            val maxClippingValue = 255 * params.clippingThreshold
            listener.invoke(params.clippingThreshold < 1f && clippingValue >= maxClippingValue)
            lastDecodeTime = currentTime
        }
        image.close()
    }

    companion object {
        private const val RECOGNIZE_ONCE_PER_MS = 500
    }
}