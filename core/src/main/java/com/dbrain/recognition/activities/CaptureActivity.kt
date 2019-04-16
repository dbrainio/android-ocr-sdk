package com.dbrain.recognition.activities

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.dbrain.recognition.camera.Camera
import com.dbrain.recognition.camera.CropParameters
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.processors.Drawer
import com.dbrain.recognition.processors.Processor
import com.dbrain.recognition.services.UploaderService
import com.dbrain.recognition.views.AutoFitTextureView
import com.dbrain.recognition.views.OverlayView
import com.dbrain.recognition.views.PhotoPreview
import com.dbrain.recognition.views.ResultScreen

abstract class CaptureActivity : AppCompatActivity(),
    TextureView.SurfaceTextureListener,
    Camera.CameraListener,
    Processor.Listener,
    View.OnClickListener,
    PhotoPreview.Listener,
    ResultScreen.Listener {

    private var textureView: AutoFitTextureView? = null
    private var overlayView: OverlayView? = null
    private var rootLayout: FrameLayout? = null
    private var titleView: TextView? = null
    private var errorDescriptionView: TextView? = null
    private var shutterButton: Button? = null
    private var preview: PhotoPreview? = null
    private var resultScreen: ResultScreen? = null

    private var camera: Camera? = null
    private var drawer: Drawer? = null
    private var processor: Processor? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.getStringExtra(UploaderService.BROADCAST_STATUS)) {
                UploaderService.BROADCAST_STATUS_ERROR -> uploadingError()
                UploaderService.BROADCAST_STATUS_COMPLETED -> uploadingCompleted()
            }
        }
    }

    abstract fun getDrawer(): Drawer?
    abstract fun getProcessor(parameters: Bundle?): Processor?
    abstract fun getCameraFacing(): Int
    abstract fun getCropRegionParameters(): CropParameters?

    protected fun getRootLayout(): FrameLayout? = rootLayout
    protected fun getTitleView(): TextView? = titleView
    protected fun getErrorDescriptionView(): TextView? = errorDescriptionView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        textureView = findViewById(R.id.texture_view)
        overlayView = findViewById(R.id.overlay_view)
        rootLayout = findViewById(R.id.root_layout)
        titleView = findViewById(R.id.title)
        errorDescriptionView = findViewById(R.id.error)
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

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter(UploaderService.BROADCAST))
    }

    protected fun stopCamera() {
        camera?.stopPreview()
    }

    protected fun startCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA
                )
                return
            }
        }

        camera?.startPreview(
            textureView,
            getCameraFacing()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        stopService(Intent(this, UploaderService::class.java))
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


        if (preview != null) rootLayout?.removeView(preview)
        preview = PhotoPreview(
            this,
            byteArray = byteArray,
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
        stopService(Intent(this, UploaderService::class.java))
    }

    override fun onBackPressed() {
        when {
            preview != null -> onPhotoPreviewBackPressed()
            resultScreen != null -> removeResultScreen()
            else -> super.onBackPressed()
        }
    }

    override fun onPhotoPreviewSendPressed(byteArray: ByteArray) {
        startService(Intent(this, UploaderService::class.java).run {
            action = UploaderService.ACTION_UPLOAD_IMAGE
            putExtra(UploaderService.ARG_IMAGE, byteArray)
            this
        })
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

    private fun uploadingError() {
        onPhotoPreviewBackPressed()
        showResultScreen(ResultScreen.RESULT_ERROR)
    }

    private fun uploadingCompleted() {
        onPhotoPreviewBackPressed()
        showResultScreen(ResultScreen.RESULT_OK)
    }

    override fun onResultScreenButtonClicked(result: Int) {
        when (result) {
            ResultScreen.RESULT_ERROR -> removeResultScreen()
            ResultScreen.RESULT_OK -> {
                setResult(Activity.RESULT_OK)
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
    }
}
