package com.dbrain.recognition.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import com.dbrain.recognition.R
import okhttp3.*


class UploaderService : IntentService("UploaderService") {
    private var okHttpClient: OkHttpClient? = null

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            ACTION_UPLOAD_IMAGE -> uploadImage(intent.getByteArrayExtra(ARG_IMAGE))
            ACTION_CANCEL -> cancel()
        }
    }

    private fun uploadImage(byteArray: ByteArray) {
        val notification = getNotificationRendererService(this)
        startForeground(1, notification)

        okHttpClient = OkHttpClient()
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"img.jpg\""),
                    RequestBody.create(MediaType.parse("image/jpeg"), byteArray)
                )
                .build()

            val request = Request.Builder()
                .header("Authorization", "Token FppN2UFV4hEJ7GhqwU24")
                .url("https://passports.outer.dbrain.io/predict")
                .post(requestBody)
                .build()

            val response = okHttpClient?.newCall(request)?.execute() ?: return

            if (!response.isSuccessful) {
                LocalBroadcastManager.getInstance(this@UploaderService).sendBroadcast(
                    Intent(BROADCAST).apply {
                        putExtra(BROADCAST_STATUS, BROADCAST_STATUS_ERROR)
                    }
                )
            } else {
                LocalBroadcastManager.getInstance(this@UploaderService).sendBroadcast(
                    Intent(BROADCAST).apply {
                        putExtra(BROADCAST_STATUS, BROADCAST_STATUS_COMPLETED)
                    }
                )
            }

            System.out.println(response.body()?.string())
        } catch (e: Exception) {

        } finally {
            stopForeground(true)
        }

    }

    private fun cancel() {
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        okHttpClient?.dispatcher()?.cancelAll()
        stopForeground(true)
    }

    private fun getNotificationRendererService(context: Context): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val groupName = getString(R.string.notification_channel)
            val mainChannel = NotificationChannel("notifications", groupName, NotificationManager.IMPORTANCE_MIN)
            mainChannel.enableLights(true)
            mainChannel.enableVibration(true)
            mainChannel.lightColor = Color.RED
            mainChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(mainChannel)
        }

        return NotificationCompat.Builder(context, "notifications")
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.uploading))
            .setSmallIcon(R.drawable.ic_stat_upload)
            .setColor(ContextCompat.getColor(context, R.color.app_red))
            .setOngoing(true)
            .setProgress(100, 0, false)
            .build()
    }

    companion object {
        const val ACTION_UPLOAD_IMAGE = "UPLOAD_IMAGE"
        const val ACTION_CANCEL = "CANCEL"

        const val ARG_IMAGE = "IMAGE"

        const val BROADCAST = "UPLOADER_SERVICE_BROADCAST"
        const val BROADCAST_STATUS = "BROADCAST_STATUS"
        const val BROADCAST_STATUS_ERROR = "BROADCAST_ERROR"
        const val BROADCAST_STATUS_COMPLETED = "BROADCAST_COMPLETED"
    }
}