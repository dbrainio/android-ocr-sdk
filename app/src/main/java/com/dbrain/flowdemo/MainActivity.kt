package com.dbrain.flowdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.dbrain.flow.Status
import com.dbrain.flow.flows.DocumentFlow
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.models.FlowClassifyResponse
import com.dbrain.flowdemo.activities.ErrorActivity
import com.dbrain.flowdemo.activities.ImagePreviewActivity
import com.dbrain.flowdemo.activities.ImagePreviewActivity.Companion.ARG_FLOW_TYPE
import com.dbrain.flowdemo.activities.RecognizeResultActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        viewModel.loadingStatus.observe(this) {
            progress_bar?.isVisible = it == Status.LOADING
            progress_description?.visibility = progress_bar?.visibility ?: View.GONE
            take_pic_button?.isVisible = progress_bar?.visibility == View.GONE
        }

        viewModel.events.observe(this) { event ->
            event.handle {
                when (event) {
                    is PhotoTakenEvent -> viewModel.classify(this)
                    is ClassifiedEvent -> {
                        val croppedPhoto = extractCroppedImage(event.result)
                        viewModel.setPhotoFile(croppedPhoto)
                        ImagePreviewActivity.show(
                            this,
                            croppedPhoto,
                            viewModel.getFlowType(),
                            REQUEST_CODE_NEED_RECOGNIZE
                        )
                    }
                    is RecognizedEvent -> RecognizeResultActivity.show(this, viewModel.getPhotoFile() ?: return@handle, event.result)
                    is ErrorEvent -> ErrorActivity.show(this, event.throwable)
                }
            }
        }

        take_pic_button?.setOnClickListener {
            viewModel.setFlowType(DocumentFlow())
            viewModel.takePicture(this)
        }
    }

    private fun extractCroppedImage(result: FlowClassifyResponse): File {
        if (result.items.isNullOrEmpty()) return viewModel.getPhotoFile()!!
        val document = result.items?.get(0)?.crop ?: return viewModel.getPhotoFile()!!
        val mediaDir = externalMediaDirs.firstOrNull()?.let { File(it, "DBrain").apply { mkdirs() } }
        val dir = if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
        val file = File(dir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg")

        var bos: BufferedOutputStream? = null
        try {
            val encodedImage = document
                .replace("data:image/png;base64,", "")
                .replace("data:image/jpeg;base64", "")

            val decodedBitmap = Base64.decode(encodedImage, Base64.DEFAULT)
            bos = BufferedOutputStream(FileOutputStream(file))
            bos.write(decodedBitmap)
            bos.flush()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            bos?.close()
        }
        return file
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_NEED_RECOGNIZE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val newFlowType = data.getParcelableExtra<FlowType>(ARG_FLOW_TYPE) ?: return
                    viewModel.setFlowType(newFlowType)
                    viewModel.recognize(this)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_NEED_RECOGNIZE = 1
    }
}