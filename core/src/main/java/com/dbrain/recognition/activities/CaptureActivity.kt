package com.dbrain.recognition.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import com.dbrain.recognition.R
import com.dbrain.recognition.api.ClassifyRequest
import com.dbrain.recognition.api.internal.Request
import com.dbrain.recognition.camera.Camera
import com.dbrain.recognition.camera.CropParameters
import com.dbrain.recognition.data.ClassifiedItem
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Drawer
import com.dbrain.recognition.processors.Processor
import com.dbrain.recognition.views.AutoFitTextureView
import com.dbrain.recognition.views.OverlayView
import com.dbrain.recognition.views.PhotoPreview
import com.dbrain.recognition.views.ResultScreen
import java.io.File
import java.lang.Exception

abstract class CaptureActivity : Activity(),
    TextureView.SurfaceTextureListener,
    Camera.CameraListener,
    Processor.Listener,
    View.OnClickListener,
    PhotoPreview.Listener,
    ResultScreen.Listener,
    Request.Callback<ArrayList<ClassifiedItem>> {

    private var textureView: AutoFitTextureView? = null
    private var overlayView: OverlayView? = null
    private var rootLayout: FrameLayout? = null
    private var shutterButton: Button? = null
    private var preview: PhotoPreview? = null
    private var resultScreen: ResultScreen? = null

    private var camera: Camera? = null
    private var drawer: Drawer? = null
    private var processor: Processor? = null
    private var classifyRequest: ClassifyRequest? = null

    abstract fun getDrawer(): Drawer?
    abstract fun getProcessor(parameters: Bundle?): Processor?
    abstract fun getCameraFacing(): Int
    abstract fun getCropRegionParameters(): CropParameters?

    protected fun getRootLayout(): FrameLayout? = rootLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        textureView = findViewById(R.id.texture_view)
        overlayView = findViewById(R.id.overlay_view)
        rootLayout = findViewById(R.id.root_layout)
        shutterButton = findViewById(R.id.shutter_button)
        shutterButton?.setOnClickListener(this)

        processor = getProcessor(intent.extras)
        drawer = getDrawer()

        overlayView?.drawer = drawer

        val cropParameters = getCropRegionParameters() ?: return
        camera = Camera(
            this,
            processor = processor,
            cropRegionX = cropParameters.ratioWidth,
            cropRegionY = cropParameters.ratioHeight,
            cropScale = cropParameters.cropScale,
            cameraListener = this
        )
        textureView?.apply {
            surfaceTextureListener = this@CaptureActivity
        }
    }

    private fun stopCamera() {
        camera?.stopPreview()
    }

    private fun startCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
            return
        }

        camera?.startPreview(
            textureView,
            getCameraFacing()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        classifyRequest?.cancel()
        processor?.releaseProcessor()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            for (r in grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    finish()
                    return
                }
                startCamera()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopCamera()
    }

    override fun onStart() {
        super.onStart()
        startCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        startCamera()
    }

    override fun onCameraStarted(previewWidth: Int, previewHeight: Int) {

    }

    override fun onCameraCropRegionSizeSet(cropWidth: Int, cropHeight: Int) {
        overlayView?.setRegion(cropWidth, cropHeight)
    }

    override fun onProcessorEvent(dataBundle: DataBundle) {
        shutterButton?.isEnabled = dataBundle.detected
        drawer?.receiveEvent(dataBundle)
        overlayView?.invalidate()
    }

    override fun onImageCaptured(byteArray: ByteArray?) {
        if (byteArray == null) return
        val fileName = File(cacheDir, "image.jpg")
        fileName.writeBytes(byteArray)

        if (preview != null) rootLayout?.removeView(preview)
        preview = PhotoPreview(
            this,
            fileName = fileName.toString(),
            facing = getCameraFacing(),
            listener = this
        )
        rootLayout?.addView(preview)
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.shutter_button -> camera?.takePicture()
        }
    }

    override fun onPhotoPreviewBackPressed() {
        if (preview != null) rootLayout?.removeView(preview)
        preview = null
        startCamera()
        classifyRequest?.cancel()
    }

    override fun onBackPressed() {
        when {
            preview != null -> onPhotoPreviewBackPressed()
            resultScreen != null -> removeResultScreen()
            else -> super.onBackPressed()
        }
    }

    override fun onPhotoPreviewSendPressed(fileName: String) {
        classifyRequest?.cancel()
        classifyRequest = ClassifyRequest(File(fileName))
        classifyRequest?.responseUI(this)
    }

    private fun showResultScreen(result: Int) {
        removeResultScreen()
        resultScreen = ResultScreen(this, result, this)
        rootLayout?.addView(resultScreen)
    }

    private fun removeResultScreen() {
        if (resultScreen != null) rootLayout?.removeView(resultScreen)
        resultScreen = null
        startCamera()
    }

    override fun onResponse(response: ArrayList<ClassifiedItem>) {
        onPhotoPreviewBackPressed()
        setResult(RESULT_OK, Intent().apply {
            putExtra(ARG_SUCCESSFULLY, true)
            putExtra(ARG_FILE, classifyRequest!!.getFile()!!.toString())
            putParcelableArrayListExtra(ARG_CLASSIFIED_LIST, response)
        })
        finish()
    }

    override fun onError(exception: Exception) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ARG_SUCCESSFULLY, false)
        })
        finish()
    }

    override fun onResultScreenButtonClicked(result: Int) {
        when (result) {
            ResultScreen.RESULT_ERROR -> removeResultScreen()
            ResultScreen.RESULT_OK -> {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CAMERA = 0

        const val ARG_CAMERA_FACING = "CAMERA_FACING"
        const val ARG_CROP_REGION_X = "CROP_REGION_X"
        const val ARG_CROP_REGION_Y = "CROP_REGION_Y"
        const val ARG_CROP_SCALE = "CROP_SCALE"
        const val ARG_FILE = "FILE"
        const val ARG_CLASSIFIED_LIST = "CLASSIFIED_LIST"
        const val ARG_SUCCESSFULLY = "SUCCESSFULLY"
    }
}
