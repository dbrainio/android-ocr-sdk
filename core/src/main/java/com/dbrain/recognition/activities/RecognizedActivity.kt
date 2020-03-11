package com.dbrain.recognition.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.dbrain.recognition.api.API
import com.dbrain.recognition.data.ClassifiedItem
import com.dbrain.recognition.services.ClassifiedUploaderService
import com.dbrain.recognition.services.RecognizedUploadService
import java.lang.Exception

class RecognizedActivity : AppCompatActivity() {

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.getStringExtra(RecognizedUploadService.RECOGNIZED_BROADCAST_STATUS)) {
                RecognizedUploadService.RECOGNIZED_BROADCAST_STATUS_ERROR -> {
                    Toast.makeText(this@RecognizedActivity, "error", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    companion object {
        private const val TYPE = "TYPE"
        private const val FILE_NAME = "file_name"

        fun go(context: Context, type: String, fileName: String) {
            context.startActivity(Intent(context, RecognizedActivity::class.java).apply{
                putExtra(TYPE, type)
                putExtra(FILE_NAME, fileName)
            })
        }
    }
}