package com.dbrain.flowdemo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.dbrain.decodeBitmap
import com.dbrain.flow.flows.DriverLicenceFlow
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.flows.PassportFlow
import com.dbrain.flowdemo.R
import com.dbrain.getFlowTypeByPosition
import com.dbrain.getFlowTypesTitles
import kotlinx.android.synthetic.main.activity_image_preview.*
import java.io.File

class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val file = intent?.extras?.getSerializable(ARG_FILE) as? File
        val currentFlowType = intent?.extras?.get(ARG_FLOW_TYPE) as? FlowType

        if (file == null || currentFlowType == null) {
            finish()
            return
        }

        image?.setImageBitmap(decodeBitmap(file))

        recognize_button?.setOnClickListener {
            val type = getFlowTypeByPosition(spinner?.selectedItemPosition ?: 0)
            val resultIntent = Intent().apply {
                putExtra(ARG_FLOW_TYPE, type)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        spinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getFlowTypesTitles()).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner?.setSelection(when (currentFlowType.name) {
            PassportFlow.NAME -> 0
            DriverLicenceFlow.NAME -> 1
            else -> 2
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val ARG_FILE = "file"
        const val ARG_FLOW_TYPE = "flow_type"

        fun show(context: Activity, file: File, currentFlowType: FlowType, requestCode: Int) {
            val intent = Intent(context, ImagePreviewActivity::class.java)
            intent.putExtra(ARG_FILE, file)
            intent.putExtra(ARG_FLOW_TYPE, currentFlowType)
            context.startActivityForResult(intent, requestCode)
        }
    }
}