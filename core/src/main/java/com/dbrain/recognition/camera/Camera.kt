package com.dbrain.recognition.camera

import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import com.dbrain.recognition.processors.Processor
import com.dbrain.recognition.views.AutoFitTextureView
import com.dbrain.recognition.views.OverlayView
import java.io.ByteArrayOutputStream
import java.util.*

class Camera(
    val context: Context,
    val processor: Processor? = null,
    val cropRegionX: Float = 1f,
    val cropRegionY: Float = 1f,
    val cropScale: Float = 0.8f,
    val cameraListener: CameraListener? = null
) : android.hardware.Camera.PreviewCallback,
    android.hardware.Camera.PictureCallback {

    interface CameraListener {
        fun onCameraStarted(previewWidth: Int, previewHeight: Int)

        fun onImageCaptured(byteArray: ByteArray?)

        fun onCameraCropRegionSizeSet(cropWidth: Int, cropHeight: Int)
    }

    private var previewSize: Size? = null
    private var cropSize: Size? = null
    private var frameIndex = 0
    private var camera: android.hardware.Camera? = null
    private var stopRunnable: Runnable? = null
    private var startRunnable: Runnable? = null
    private val handler = Handler(context.mainLooper)
    private val backgroundThread = DispatchQueue("CameraThread")
    private var displayOrientation = 0


    override fun onPreviewFrame(byteArray: ByteArray?, camera: Camera?) {
        if (byteArray == null) return
        if (previewSize != null) {
            if (cropSize != null && cropRegionX > 0 && cropRegionY > 0) {
                if (frameIndex % 10 == 0) {
                    val time = System.currentTimeMillis()
                    val parameters = camera?.parameters ?: return
                    val yuv =
                        YuvImage(byteArray, parameters.previewFormat, previewSize!!.width, previewSize!!.height, null)
                    val out = ByteArrayOutputStream()
                    val rect = Rect(
                        previewSize!!.width / 2 - cropSize!!.width / 2,
                        previewSize!!.height / 2 - cropSize!!.height / 2,
                        previewSize!!.width / 2 + cropSize!!.width / 2,
                        previewSize!!.height / 2 + cropSize!!.height / 2
                    )
                    yuv.compressToJpeg(rect, 30, out)
                    val bytes = out.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    processor?.receiveFrame(bitmap, byteArray, previewSize!!.width, previewSize!!.height, frameIndex)
                    Log.e("proceesed", "time = " + (System.currentTimeMillis() - time))

//                    var currentPos = 0
//                    val croppedOutput = ByteArray(rect.width() * rect.height())
//                    for (y in 0 until previewSize!!.height) {
//                        for (x in 0 until previewSize!!.width) {
//                            if (rect.contains(x, y)) {
//                                croppedOutput[currentPos] = byteArray[x * y]
//                            }
//                        }
//                    }
//                    Log.e("proceesed", "time = " + (System.currentTimeMillis() - time))
//                    processor?.receiveFrame(null, byteArray, previewSize!!.width, previewSize!!.height, frameIndex)
                }
            } else {
                processor?.receiveFrame(null, byteArray, previewSize!!.width, previewSize!!.height, frameIndex)
            }
        }
        frameIndex++
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        if (data == null) return

        handler.post {
            cameraListener?.onImageCaptured(data)
        }
    }

    fun takePicture() {
        camera?.takePicture(null, null, this)
    }

    fun startPreview(
        textureView: AutoFitTextureView?,
        cameraFacing: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
    ) {
        if (textureView == null) return

        if (stopRunnable != null) {
            backgroundThread.cancelRunnable(stopRunnable)
            stopRunnable = null
        }
        if (startRunnable != null) {
            backgroundThread.cancelRunnable(startRunnable)
            startRunnable = null
        }
        startRunnable = Runnable {
            camera = createCameraPreview(textureView, cameraFacing)
            if (camera == null) onCameraError(textureView)
            startRunnable = null
        }
        backgroundThread.postRunnable(startRunnable, 300)
    }

    fun stopPreview() {
        if (stopRunnable != null) {
            backgroundThread.cancelRunnable(stopRunnable)
            stopRunnable = null
        }
        if (startRunnable != null) {
            backgroundThread.cancelRunnable(startRunnable)
            startRunnable = null
        }
        if (camera != null) {
            stopRunnable = Runnable {
                releaseCamera()
                stopRunnable = null
            }
            backgroundThread.postRunnable(stopRunnable)
        }
    }

    private fun createCameraPreview(
        textureView: AutoFitTextureView,
        cameraFacing: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
    ): android.hardware.Camera? {
        try {
            val surface = textureView.surfaceTexture ?: return null
            if (camera != null) {
                return try {
                    surface.setDefaultBufferSize(previewSize?.width ?: 0, previewSize?.height ?: 0)
                    camera?.apply {
                        setPreviewTexture(surface)
                        setPreviewCallback(this@Camera)
                        startPreview()
                    }
                    handler.post {
                        if (previewSize != null) {
                            onCameraStarted(previewSize!!.width, previewSize!!.height)
                        }
                    }
                    camera
                } catch (e: Throwable) {
                    null
                }
            }
            var backCamera: android.hardware.Camera? =
                android.hardware.Camera.open(cameraFacing)
            if (backCamera == null) {
                backCamera = android.hardware.Camera.open()
            }
            val parameters = backCamera!!.parameters
            if (parameters.supportedFocusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.focusMode = android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            } else if (parameters.supportedFocusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.focusMode = android.hardware.Camera.Parameters.FOCUS_MODE_AUTO
            }

            previewSize = getPreviewSize(
                toUniversalSize(parameters.supportedPreviewSizes),
                textureView.width
            )

            var textureWidth = textureView.width
            var textureHeight = textureView.height
            if (textureWidth <= textureHeight) {
                textureWidth = textureHeight * previewSize!!.height / previewSize!!.width
            } else {
                textureHeight = textureWidth * previewSize!!.width / previewSize!!.height
            }
            val xScale = (textureView.width.toFloat() / textureWidth.toFloat())
            val yScale = (textureView.height.toFloat() / textureHeight.toFloat())

            val previewAspect = previewSize!!.width.toFloat() / previewSize!!.height.toFloat()
            val textureAspect = textureView.width.toFloat() / textureView.height.toFloat()

            val requestedPreviewAspect =
                if (cropRegionX <= 0 || cropRegionY <= 0) previewAspect else cropRegionY / cropRegionX
            val requestedTextureAspect =
                if (cropRegionX <= 0 || cropRegionY <= 0) textureAspect else cropRegionX / cropRegionY
            val resultPreviewAspect = requestedPreviewAspect / previewAspect
            val resultTextureAspect = requestedTextureAspect / textureAspect

            val regionW: Int
            val regionH: Int

            if (cropRegionX >= cropRegionY) {
                cropSize = Size(
                    (previewSize!!.width * resultPreviewAspect * yScale * cropScale).toInt(),
                    (previewSize!!.height * xScale * cropScale).toInt()
                )
                regionW = (textureView.width * cropScale).toInt()
                regionH = ((textureView.height / resultTextureAspect) * cropScale).toInt()
            } else {
                cropSize = Size(
                    (previewSize!!.width * yScale * cropScale).toInt(),
                    ((previewSize!!.height / resultPreviewAspect) * xScale * cropScale).toInt()
                )
                regionW = (textureView.width * resultTextureAspect * cropScale).toInt()
                regionH = (textureView.height * cropScale).toInt()
            }

            parameters.setPreviewSize(previewSize?.width ?: 0, previewSize?.height ?: 0)
            val info = android.hardware.Camera.CameraInfo()
            android.hardware.Camera.getCameraInfo(cameraFacing, info)
            backCamera.setDisplayOrientation(getOrientation(info))
            displayOrientation = getDisplayOrientation()
            val previewFpsRange = IntArray(2)
            parameters.getPreviewFpsRange(previewFpsRange)
            if (previewFpsRange[0] == previewFpsRange[1]) {
                val supportedFpsRanges = parameters.supportedPreviewFpsRange
                for (range in supportedFpsRanges) {
                    if (range[0] != range[1]) {
                        parameters.setPreviewFpsRange(range[0], range[1])
                        break
                    }
                }
            }

            val sizes = parameters.supportedPictureSizes
            sizes.sortWith(Comparator { o1, o2 -> o1.width - o2.width })
            var size: android.hardware.Camera.Size = sizes[0]
            if (sizes.size > 1) {
                for (s in sizes) {
                    if (s.width > 1280) break
                    if (s.height >= size.height || s.width > size.width)
                        size = s
                }
            }
            parameters.setPictureSize(size.width, size.height)
            backCamera.parameters = parameters
            surface.setDefaultBufferSize(previewSize?.width ?: 0, previewSize?.height ?: 0)

            return try {
                backCamera.setPreviewTexture(surface)
                backCamera.setPreviewCallback(this@Camera)
                backCamera.startPreview()
                handler.post {
                    cameraListener?.onCameraCropRegionSizeSet(regionW, regionH)
                    if (previewSize != null) {
                        textureView.setAspectRatio(previewSize!!.height, previewSize!!.width)
                        onCameraStarted(previewSize?.width ?: 0, previewSize?.height ?: 0)
                    }
                }
                backCamera
            } catch (e: Throwable) {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getPreviewSize(sizes: List<Size>, viewWidth: Int): Size {
        val minSizes = ArrayList<Size>()
        for (size in sizes) {
            if (viewWidth >= size.width) {
                minSizes.add(size)
            }
        }
        if (minSizes.size >= 2) {
            val firstSize = minSizes[0]
            val lastSize = minSizes[minSizes.size - 1]
            return if (firstSize.width > lastSize.width) {
                firstSize
            } else lastSize
        }
        return minSizes[minSizes.size - 1]
    }

    private fun toUniversalSize(sizes: List<android.hardware.Camera.Size>): List<Size> {
        val list = ArrayList<Size>()
        for (size in sizes) {
            list.add(Size(size.width, size.height))
        }
        return list
    }

    private fun getOrientation(info: android.hardware.Camera.CameraInfo): Int {
        if ("samsung" == Build.MANUFACTURER && "sf2wifixx" == Build.PRODUCT) {
            return 0
        }
        val displayOrientation = (info.orientation - getDisplayOrientation() + 360) % 360
        var degrees = 0
        var temp = displayOrientation
        when (temp) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        if (info.orientation % 90 != 0) {
            info.orientation = 0
        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            temp = (info.orientation + degrees) % 360;
            temp = (360 - temp) % 360;
        } else {
            temp = (info.orientation - degrees + 360) % 360;
        }
        return temp
    }

    private fun getDisplayOrientation(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val rotation = windowManager?.defaultDisplay?.rotation ?: return 0
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        return degrees
    }

    private fun onCameraError(textureView: AutoFitTextureView?) {
        handler.post {
            startPreview(textureView)
        }
    }

    private fun onCameraStarted(previewWidth: Int, previewHeight: Int) {
        handler.post {
            cameraListener?.onCameraStarted(previewWidth, previewHeight)
        }
    }

    private fun releaseCamera() {
        previewSize = null
        if (camera != null) {
            camera?.stopPreview()
            camera?.setPreviewCallback(null)
            try {
                camera?.setPreviewTexture(null)
            } catch (ignored: Throwable) {
            }
            camera?.release()
            camera = null
        }
    }
}