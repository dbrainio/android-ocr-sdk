package com.dbrain.recognition.services

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.dbrain.recognition.api.API

class RecognizedUploadService : UploaderService("RecognizedUploadService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            ACTION_UPLOAD_IMAGE -> uploadImage(
                intent.getStringExtra(ARG_TYPE),
                intent.getStringExtra(ARG_FILE_NAME)
            )
           ACTION_CANCEL -> cancel()
        }
    }

    private fun uploadImage(type: String, fileName: String) {
        log("start upload image $fileName")
        start()
        try {
            val recognized = API.recognize(type, fileName)
            log("recognized image")
            LocalBroadcastManager.getInstance(this@RecognizedUploadService).sendBroadcast(
                Intent(RECOGNIZED_BROADCAST).apply {
                    putParcelableArrayListExtra(RECOGNIZED_BROADCAST_RESULT_ITEMS, recognized)
                    putExtra(
                        RECOGNIZED_BROADCAST_STATUS,
                        RECOGNIZED_BROADCAST_STATUS_COMPLETED
                    )
                }
            )
        } catch (e: Exception) {
            loge(e)
            LocalBroadcastManager.getInstance(this@RecognizedUploadService).sendBroadcast(
                Intent(RECOGNIZED_BROADCAST).apply {
                    putExtra(
                        RECOGNIZED_BROADCAST_STATUS,
                        RECOGNIZED_BROADCAST_STATUS_ERROR
                    )
                }
            )
        } finally {
            finish()
        }
    }

    companion object {
        const val ACTION_UPLOAD_IMAGE = "UPLOAD_IMAGE"
        const val ACTION_CANCEL = "CANCEL"

        const val ARG_FILE_NAME = "FILE_NAME"
        const val ARG_TYPE = "TYPE"

        const val RECOGNIZED_BROADCAST = "RECOGNIZED_UPLOADER_SERVICE_BROADCAST"
        const val RECOGNIZED_BROADCAST_STATUS = "RECOGNIZED_BROADCAST_STATUS"
        const val RECOGNIZED_BROADCAST_STATUS_ERROR = "RECOGNIZED_BROADCAST_ERROR"
        const val RECOGNIZED_BROADCAST_STATUS_COMPLETED = "RECOGNIZED_BROADCAST_COMPLETED"
        const val RECOGNIZED_BROADCAST_RESULT_ITEMS = "RECOGNIZED_BROADCAST_RESULT_ITEMS"
    }

}