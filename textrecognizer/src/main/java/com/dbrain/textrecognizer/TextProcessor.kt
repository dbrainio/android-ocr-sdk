package com.dbrain.textrecognizer

import android.content.Context
import android.graphics.*
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Processor
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class TextProcessor(
    context: Context,
    listener: Listener
) : Processor(context, listener) {

    companion object {

        //Для вычисления гистограммы относительно самой встречающейся яркости. Можно считать, что в гистограмме значения от 0 до 1, просто дабы не связываться с флоатами,
        // работаем со значениями от 0 до 1000.
        private const val MOST_FREQUENT_LUMINOCITY_BASE_VALUE = 100000

        //Нижний порог яркости, которую мы можем рассматривать как засвеченное изображение
        private const val BRIGHT_SPOT_LUMINOCITY_LOWER_BOUND = 235
        //Если ярких пикселей — больше 40% от самых встречающихся, считаем, что на изображении есть засветы
        private const val BRIGHT_VALUES_RELATIVE_VALUE_UPPER_BOUND = (0.05 * MOST_FREQUENT_LUMINOCITY_BASE_VALUE).toInt()
    }

    private val dataBundle = DataBundle()

    //Для проверки на засветы
    private val actualFrameHistogram = IntArray(256)

    override fun close() {

    }

    override fun processFrame(
        croppedBitmap: Bitmap?,
        originalByteArray: ByteArray?,
        cameraPreviewWidth: Int,
        cameraPreviewHeight: Int,
        frameIndex: Int
    ) {

        val frame = Frame()
        if (croppedBitmap != null) {
            frame.bitmap = croppedBitmap
        } else {
            frame.byteBuffer = ByteBuffer.wrap(originalByteArray, 0, originalByteArray!!.size)
            frame.width = cameraPreviewWidth
            frame.height = cameraPreviewHeight
        }

        getFrameHistogram(frame, actualFrameHistogram)

        dataBundle.detected = !containsBrightAreas(actualFrameHistogram, BRIGHT_SPOT_LUMINOCITY_LOWER_BOUND, BRIGHT_VALUES_RELATIVE_VALUE_UPPER_BOUND)
        postEvent(dataBundle)
        if (BuildConfig.DEBUG) {
            sendHistogram()
        }
    }

    private fun sendHistogram() {
        val histogramCopy = IntArray(actualFrameHistogram.size) { actualFrameHistogram[it] }
        val data = HistogramDataBundle(histogramCopy, BRIGHT_SPOT_LUMINOCITY_LOWER_BOUND, BRIGHT_VALUES_RELATIVE_VALUE_UPPER_BOUND)
        postEvent(data)
    }

    private val yValuesCount = IntArray(256)                            //Индекс = яркость от 0 до 255, значение — сколько пикселей с такой яркостью встречается на кропе
    private var mostFrequentYValue = 0

    private fun getFrameHistogram(frame: Frame, output: IntArray) {
        val yValuesBuf = frame.grayscale()
        for (i in 0 until 256) {
            yValuesCount[i] = 0
            output[i] = 0
        }
        mostFrequentYValue = 0
        yValuesBuf.rewind()
        val asArray = ByteArray(yValuesBuf.remaining())
        yValuesBuf.get(asArray)
        //В NV21 (да и по доке) первые width * height байт — яркостные составляющие каждого пикселя и только потом могут идти цветоразностные.
        //Нам цветоразностные не нужны, но есть нюанс: байт в джаве знаковый, поэтому нам нужно ещё будет перевести его, чтобы не было отрицательных яркостных значений.

        val picArea = frame.width * frame.height
        for (i in 0 until picArea) {
            val luminosityOfCurrentPixel = asArray[i].toInt().and(0xFF)         //перевод из signed byte в signed int, чтобы уж точно все поместилось

            //вообще, во время тестов значения ниже 0 и выше 255 не попадались, но на всякий случай ограничим
            val clamped = max(0, min(luminosityOfCurrentPixel, 255))
            yValuesCount[clamped]++
        }

        for (i in yValuesCount.indices) {
            //можно было бы запихнуть в цикл выше, но там будет больше сравнений
            val frequencyOfCurrentLuminocity = yValuesCount[i]
            val frequencyOfCurrentlyMostFrequent = yValuesCount[mostFrequentYValue]
            if (frequencyOfCurrentLuminocity > frequencyOfCurrentlyMostFrequent) {
                mostFrequentYValue = i
            }
        }

        val mostFrequentCount = yValuesCount[mostFrequentYValue]
        for (i in yValuesCount.indices) {
            val frequencyOfCurrentLuminocity = yValuesCount[i]
            val proportion = ((MOST_FREQUENT_LUMINOCITY_BASE_VALUE * frequencyOfCurrentLuminocity.toLong()) / mostFrequentCount).toInt()
            output[i] = proportion
        }
    }

    private fun containsBrightAreas(histogram: IntArray, luminocityValueLowerBound: Int, relativeValueUpperBound: Int) : Boolean {
        //В ней 100000 — это значение самой часто встречающейся яркости, все остальные — это "как часто встречается относительно самой частой".
        //Аналогия из не-фотошопа: современный дизайн опросов во многих соцсетях (в том числе и ВК): самый популярный ответ занимает прогресс на 100%, а не на долю голосов за вариант ответа.

        for (i in luminocityValueLowerBound until histogram.size) {
            val brightSpotRelativeFrequency = histogram[i]
            if (brightSpotRelativeFrequency > relativeValueUpperBound) {
                //Доля пикселей с высоким яркостным значением слишком большая, считаем, что засветы таки есть
                return true
            }
        }

        return false
    }

    private class Frame {

        var bitmap: Bitmap? = null
        var byteBuffer: ByteBuffer? = null
        var width: Int = 0
        get() {
            return bitmap?.width ?: field
        }

        var height: Int = 0
        get() {
            return bitmap?.height ?: field
        }

        fun grayscale(): ByteBuffer {
            if (byteBuffer != null) {
                return byteBuffer!!
            }
            val pixels = IntArray(width * height)
            bitmap!!.getPixels(pixels, 0, width, 0, 0, width, height)
            val grayscalePixels = ByteArray(width * height)
            var pixedIndex = 0
            while(pixedIndex < pixels.size) {
                grayscalePixels[pixedIndex] = (Color.red(pixels[pixedIndex]).toFloat() * 0.299F + Color.green(pixels[pixedIndex]).toFloat() * 0.587F + Color.blue(pixels[pixedIndex]).toFloat() * 0.114F).toByte()
                pixedIndex++
            }
            return ByteBuffer.wrap(grayscalePixels)
        }
    }
}
