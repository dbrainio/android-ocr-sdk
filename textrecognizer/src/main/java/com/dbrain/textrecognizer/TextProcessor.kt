package com.dbrain.textrecognizer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import com.dbrain.recognition.DBrainBuilder
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Processor
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class TextProcessor(
    context: Context,
    parameters: Bundle?,
    listener: Listener
) : Processor(context, listener),
    Detector.Processor<TextBlock> {

    companion object {

        //Для вычисления гистограммы относительно самой встречающейся яркости. Можно считать, что в гистограмме значения от 0 до 1, просто дабы не связываться с флоатами,
        // работаем со значениями от 0 до 1000.
        private const val MOST_FREQUENT_LUMINOCITY_BASE_VALUE = 100000

        //Нижний порог яркости, которую мы можем рассматривать как засвеченное изображение
        private const val BRIGHT_SPOT_LUMINOCITY_LOWER_BOUND = 235
        //Если ярких пикселей — больше 40% от самых встречающихся, считаем, что на изображении есть засветы
        private const val BRIGHT_VALUES_RELATIVE_VALUE_UPPER_BOUND = (0.05 * MOST_FREQUENT_LUMINOCITY_BASE_VALUE).toInt()
    }


    private var textRecognizer: TextRecognizer? = null
    private val MIN_ALLOWED_TEXT_LENGTH = 20
    private var detected = true
    private val frameBuilder = Frame.Builder()
    private val rotation =
        if (parameters?.getInt(CaptureActivity.ARG_CROP_SCALE) == DBrainBuilder.CAMERA_FACING_BACK) 1 else 3
    private val dataBundle = DataBundle()

    //Для проверки на засветы

    private val actualFrameHistogram = IntArray(256)


    override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
        val items = detections.detectedItems
        var textLength = 0
        for (i in 0 until items.size()) {
            val item = items.valueAt(i)
            textLength += item.value.length
            if (textLength >= MIN_ALLOWED_TEXT_LENGTH) break
        }

        val detected = textLength >= MIN_ALLOWED_TEXT_LENGTH

        if (BuildConfig.DEBUG) {
            sendHistogram(detected)
        }

        if (this.detected != detected) {
            this.detected = detected
            dataBundle.detected = detected
            postEvent(dataBundle)
        }
    }

    override fun release() {

    }

    override fun close() {
        textRecognizer?.release()
    }

    override fun processFrame(
        croppedBitmap: Bitmap?,
        originalByteArray: ByteArray?,
        cameraPreviewWidth: Int,
        cameraPreviewHeight: Int,
        frameIndex: Int
    ) {
        if (textRecognizer == null) textRecognizer = TextRecognizer.Builder(context).build().apply {
            setProcessor(this@TextProcessor)
        }

        val frame = frameBuilder
            .setId(frameIndex)
            .setTimestampMillis(System.currentTimeMillis())
            .setRotation(rotation)

        if (croppedBitmap != null)
            frame.setBitmap(croppedBitmap)
        else
            frame.setImageData(
                ByteBuffer.wrap(originalByteArray, 0, originalByteArray!!.size),
                cameraPreviewWidth,
                cameraPreviewHeight,
                ImageFormat.NV21
            )

        val builtFrame = frame.build()


        getFrameHistogram(builtFrame, actualFrameHistogram)
        if (!containsBrightAreas(actualFrameHistogram, BRIGHT_SPOT_LUMINOCITY_LOWER_BOUND, BRIGHT_VALUES_RELATIVE_VALUE_UPPER_BOUND)) {
            textRecognizer?.receiveFrame(builtFrame)
        } else {
            dataBundle.detected = false
            postEvent(dataBundle)

            if (BuildConfig.DEBUG) {
                sendHistogram(false)
            }
        }

    }

    override fun notifyPictureTaken() = Unit

    private fun sendHistogram(detectedText: Boolean) {

        val histogramCopy = IntArray(actualFrameHistogram.size) { actualFrameHistogram[it] }
        val data = HistogramDataBundle(detectedText, histogramCopy, BRIGHT_SPOT_LUMINOCITY_LOWER_BOUND, BRIGHT_VALUES_RELATIVE_VALUE_UPPER_BOUND)
        postEvent(data)
    }


    private val yValuesCount = IntArray(256)                            //Индекс = яркость от 0 до 255, значение — сколько пикселей с такой яркостью встречается на кропе
    private var mostFrequentYValue = 0

    private fun getFrameHistogram(frame: Frame, output: IntArray) {
        val yValuesBuf = frame.grayscaleImageData
        for (i in 0 until 256) {
            yValuesCount[i] = 0
            output[i] = 0
        }
        mostFrequentYValue = 0
        yValuesBuf.rewind()
        val asArray = ByteArray(yValuesBuf.remaining())
        yValuesBuf.get(asArray)
        val metadata = frame.metadata
        //В NV21 (да и по доке) первые width * height байт — яркостные составляющие каждого пикселя и только потом могут идти цветоразностные.
        //Нам цветоразностные не нужны, но есть нюанс: байт в джаве знаковый, поэтому нам нужно ещё будет перевести его, чтобы не было отрицательных яркостных значений.

        val picArea = metadata.width * metadata.height
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
                if (BuildConfig.DEBUG) {
                    Log.d("TextProcessor", "Detected bright spots: luminocity = $i, value = $brightSpotRelativeFrequency")
                }
                return true
            }
        }

        return false
    }
}
