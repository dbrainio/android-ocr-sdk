package com.dbrain.recognition.processors

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler

abstract class Processor(val context: Context, val listener: Listener?) {
    interface Listener {
        fun onProcessorEvent(dataBundle: DataBundle)
    }

    private val handler = Handler(context.mainLooper)
    private var closed = false

    /**
     * Method for processing camera images.
     * Called on background thread.
     **/
    @Synchronized
    fun receiveFrame(bitmap: Bitmap?, byteArray: ByteArray?, previewWidth: Int, previewHeight: Int, frameIndex: Int) {
        if (!closed) processFrame(bitmap, byteArray, previewWidth, previewHeight, frameIndex)
    }

    /**
     * Call this method to free processor resources
     **/
    @Synchronized
    fun releaseProcessor() {
        closed = true
        close()
    }

    /**
     * Method to notify listener if some event happened or status changed in processor.
     **/
    protected fun postEvent(dataBundle: DataBundle) {
        handler.post { listener?.onProcessorEvent(dataBundle) }
    }

    /**
     * Free any resources in this method.
     **/
    protected abstract fun close()

    /**
     * Method to notify processor that image had been captured. May be used to reset internal state.
     **/
    abstract fun notifyPictureTaken()

    /**
     * Process image in this method.
     * @param croppedBitmap cropped region of the original image. NULL if crop region is undefined.
     * @param originalByteArray yuv data directly from camera.
     * @param cameraPreviewWidth camera image preview width
     * @param cameraPreviewHeight camera image preview height
     * Called on background thread.
     **/
    protected abstract fun processFrame(
        croppedBitmap: Bitmap?,
        originalByteArray: ByteArray?,
        cameraPreviewWidth: Int,
        cameraPreviewHeight: Int,
        frameIndex: Int
    )
}