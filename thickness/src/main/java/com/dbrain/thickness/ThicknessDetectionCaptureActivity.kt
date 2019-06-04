package com.dbrain.thickness

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.recognition.camera.Camera
import com.dbrain.recognition.camera.CropParameters
import com.dbrain.recognition.views.AutoFitTextureView

class ThicknessDetectionCaptureActivity : AppCompatActivity(),
    TextureView.SurfaceTextureListener,
    Camera.CameraListener
{
    private lateinit var textureView: AutoFitTextureView
    private lateinit var shutterButton: Button
    private lateinit var uploadingProgressBar: View

    private lateinit var resultsContainer: View
    private lateinit var resultImage: ImageView
    private lateinit var closeResults: View

    private val cameraFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK

    private var camera: Camera? = null

    private var uploading: Boolean = false
    private var inResultsScreen: Boolean = false

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.getStringExtra(ThicknessDetectionUploaderService.BROADCAST_STATUS)) {
                ThicknessDetectionUploaderService.BROADCAST_STATUS_ERROR -> uploadingError()
                ThicknessDetectionUploaderService.BROADCAST_STATUS_COMPLETED -> {
                    val resultBytes = intent.getByteArrayExtra(ThicknessDetectionUploaderService.BROADCAST_RESULT_IMAGE)
                    val bmp = if (resultBytes != null) BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.size) else null
                    if (bmp != null) {
                        uploadingCompleted()
                        showResults(bmp)
                    } else {
                        uploadingError()
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thickness_capture)
        textureView = findViewById(R.id.texture_view)
        shutterButton = findViewById<Button>(R.id.shutter_button).apply {
            isEnabled = true
        }
        resultsContainer = findViewById(R.id.results)
        resultImage = resultsContainer.findViewById(R.id.result)
        closeResults = resultsContainer.findViewById(R.id.close_results)

        shutterButton.setOnClickListener { camera?.takePicture() }
        closeResults.setOnClickListener { hideResults() }

        uploadingProgressBar = findViewById(R.id.progress)

        var _cropParameters: CropParameters? = null
        intent?.extras?.let {
            val cropX = it.getFloat(CaptureActivity.ARG_CROP_REGION_X)
            val cropY = it.getFloat(CaptureActivity.ARG_CROP_REGION_Y)
            val cropScale = it.getFloat(CaptureActivity.ARG_CROP_SCALE)
            _cropParameters = CropParameters(cropX, cropY, cropScale)
        }
        val cropParameters = _cropParameters ?: return
        camera = Camera(
            this,
            processor = null,
            cropRegionX = cropParameters.ratioWidth,
            cropRegionY = cropParameters.ratioHeight,
            cropScale = cropParameters.cropScale,
            cameraListener = this
        )
        textureView?.apply {
            surfaceTextureListener = this@ThicknessDetectionCaptureActivity
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter(ThicknessDetectionUploaderService.BROADCAST))
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
        if (uploading || inResultsScreen){
            return
        }

        camera?.startPreview(
            textureView,
            cameraFacing
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        stopService(Intent(this, ThicknessDetectionUploaderService::class.java))
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

    override fun onCameraStarted(previewWidth: Int, previewHeight: Int) = Unit

    override fun onCameraCropRegionSizeSet(cropWidth: Int, cropHeight: Int) = Unit

    override fun onImageCaptured(byteArray: ByteArray?) {
        if (byteArray == null) return
        shutterButton?.text = null
        uploadingProgressBar?.visibility = View.VISIBLE
        stopCamera()
        uploading = true
//        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//        uploadingCompleted()
//        showResults(bmp)
        startService(Intent(this, ThicknessDetectionUploaderService::class.java).run {
            action = ThicknessDetectionUploaderService.ACTION_UPLOAD_IMAGE
            putExtra(ThicknessDetectionUploaderService.ARG_IMAGE, byteArray)
            this
        })
    }

    override fun onBackPressed() {
        if (inResultsScreen) {
            hideResults()
        } else {
            super.onBackPressed()
        }
    }

    private fun uploadingError() {
        uploading = false
        shutterButton.setText(R.string.take_a_photo)
        uploadingProgressBar.visibility = View.INVISIBLE
        //TODO toast
    }

    private fun uploadingCompleted() {
        uploading = false
        shutterButton.setText(R.string.take_a_photo)
        uploadingProgressBar.visibility = View.INVISIBLE
    }

    private fun showResults(bitmap: Bitmap) {
        inResultsScreen = true
        resultsContainer.visibility = View.VISIBLE
        resultImage.setImageBitmap(bitmap)
        stopCamera()
    }

    private fun hideResults() {
        resultsContainer.visibility = View.INVISIBLE
        resultImage.setImageBitmap(null)
        inResultsScreen = false
        startCamera()
    }


    companion object {
        const val PERMISSION_REQUEST_CAMERA = 0

        const val ARG_CAMERA_FACING = "CAMERA_FACING"
        const val ARG_CROP_REGION_X = "CROP_REGION_X"
        const val ARG_CROP_REGION_Y = "CROP_REGION_Y"
        const val ARG_CROP_SCALE = "CROP_SCALE"
    }



}