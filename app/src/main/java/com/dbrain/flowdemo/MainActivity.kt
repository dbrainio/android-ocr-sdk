package com.dbrain.flowdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.dbrain.flow.Status
import com.dbrain.flow.common.NoResultException
import com.dbrain.flow.flows.DriverLicenceFlow
import com.dbrain.flow.flows.PassportFlow
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel

    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        viewModel.loadingStatus.observe(this) {
            progress_bar?.isVisible = it == Status.LOADING
        }

        viewModel.state.observe(this) {
            if (it.photoFile != null) {
                // photo captured
                showPicture(it.photoFile)
            }
            if (it.result != null) {
                // image recognized
                result_text?.text = it.result.raw
            }
            if (it.error != null) {
                // error occurred
                result_text?.text = if (it.error is NoResultException) {
                    it.result?.raw
                } else it.error.stackTraceToString()
            }
            recognize_button?.isEnabled = it.photoFile != null
        }

        take_pic_button?.setOnClickListener {
            viewModel.takePicture(
                this, DriverLicenceFlow().apply {
                    verifyFields = mapOf(
                        "first_name" to "ФОМА"
                    )
                }
            )
        }
        recognize_button?.setOnClickListener {
            photoFile?.let { viewModel.recognize(this, it) }
        }
    }

    private fun showPicture(file: File) {
        this.photoFile = file
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inSampleSize = calculateInSampleSize(options, 700, 700)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        image_view?.setImageBitmap(bitmap)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        val sWidth = Math.round(width.toFloat() / reqWidth.toFloat())
        val sHeight = Math.round(height.toFloat() / reqHeight.toFloat())
        return if (sWidth <= sHeight) sHeight else sWidth
    }

}