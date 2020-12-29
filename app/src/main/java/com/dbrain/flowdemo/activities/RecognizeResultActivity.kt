package com.dbrain.flowdemo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbrain.decodeBitmap
import com.dbrain.flow.models.FlowRecognizeResponse
import com.dbrain.flowdemo.R
import com.dbrain.flowdemo.adapters.RecognitionAdapter
import kotlinx.android.synthetic.main.activity_image_preview.*
import kotlinx.android.synthetic.main.activity_result.*
import java.io.File

class RecognizeResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val file = intent?.extras?.get(ARG_PHOTO_FILE) as? File
        val result = intent?.getParcelableExtra<FlowRecognizeResponse>(ARG_RESULT)

        if (result == null || file == null) {
            finish()
            return
        }

        recycler_view?.apply {
            adapter = RecognitionAdapter(this@RecognizeResultActivity, decodeBitmap(file), result)
            layoutManager = LinearLayoutManager(this@RecognizeResultActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val ARG_RESULT = "result"
        const val ARG_PHOTO_FILE = "photo_file"

        fun show(context: Activity, photoFile: File, result: FlowRecognizeResponse) {
            val intent = Intent(context, RecognizeResultActivity::class.java)
            intent.putExtra(ARG_RESULT, result)
            intent.putExtra(ARG_PHOTO_FILE, photoFile)
            context.startActivity(intent)
        }
    }
}