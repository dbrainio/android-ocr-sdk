package com.dbrain.recognition.services

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.dbrain.recognition.api.API

class ClassifiedUploaderService : UploaderService("ClassifiedUploaderService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            ACTION_UPLOAD_IMAGE -> uploadImage(intent.getStringExtra(ARG_FILE_NAME))
            ACTION_CANCEL -> cancel()
        }
    }

    private fun uploadImage(fileName: String) {
        log("start upload image $fileName")
        start()
        try {
            val items = API.classify(fileName)
            log("classified image")
            LocalBroadcastManager.getInstance(this@ClassifiedUploaderService).sendBroadcast(
                Intent(CLASSIFIED_BROADCAST).apply {
                    putExtra(CLASSIFIED_BROADCAST_STATUS, CLASSIFIED_BROADCAST_STATUS_COMPLETED)
                    putParcelableArrayListExtra(CLASSIFIED_BROADCAST_RESULT_ITEMS, items)
                    putExtra(CLASSIFIED_BROADCAST_RESULT_FILE_NAME, fileName)
                }
            )
        } catch (e: Exception) {
            loge(e)
            LocalBroadcastManager.getInstance(this@ClassifiedUploaderService).sendBroadcast(
                Intent(CLASSIFIED_BROADCAST).apply {
                    putExtra(CLASSIFIED_BROADCAST_STATUS, CLASSIFIED_BROADCAST_STATUS_ERROR)
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

        const val CLASSIFIED_BROADCAST = "CLASSIFIED_UPLOADER_SERVICE_BROADCAST"
        const val CLASSIFIED_BROADCAST_STATUS = "CLASSIFIED_BROADCAST_STATUS"
        const val CLASSIFIED_BROADCAST_STATUS_ERROR = "CLASSIFIED_BROADCAST_ERROR"
        const val CLASSIFIED_BROADCAST_STATUS_COMPLETED = "CLASSIFIED_BROADCAST_COMPLETED"
        const val CLASSIFIED_BROADCAST_RESULT_ITEMS = "CLASSIFIED_BROADCAST_RESULT_ITEMS"
        const val CLASSIFIED_BROADCAST_RESULT_FILE_NAME = "CLASSIFIED_BROADCAST_RESULT_FILE_NAME"
    }
}