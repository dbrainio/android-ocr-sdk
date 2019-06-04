package com.dbrain.thickness

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
import org.apache.james.mime4j.stream.EntityState
import org.apache.james.mime4j.stream.MimeTokenStream
import java.io.ByteArrayOutputStream


class ThicknessDetectionUploaderService : IntentService("ThicknessDetectionUploaderService") {
    private var okHttpClient: OkHttpClient? = null

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            ACTION_UPLOAD_IMAGE -> uploadImage(intent.getByteArrayExtra(
                ARG_IMAGE
            ))
            ACTION_CANCEL -> cancel()
        }
    }

    private val buf = ByteArray(4096)

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
                .header("Authorization", "Token 1234")
                .url("http://104.248.29.23:8000/predict")
                .post(requestBody)
                .build()

            val response = okHttpClient?.newCall(request)?.execute() ?: return

            if (!response.isSuccessful) {
                throw IllegalStateException("Unsuccessful response")
            } else {
                val stream = MimeTokenStream()
                stream.parse(response.body()?.byteStream() ?: throw NullPointerException("response.byteStream() is null"))
                var state = stream.state
                while (state != EntityState.T_END_MESSAGE) {
                    if (state == EntityState.T_BODY) {
                        val input = stream.inputStream
                        val output = ByteArrayOutputStream()
                        var bytesRead: Int = 0
                        while (bytesRead != -1) {
                            bytesRead = input.read(buf, 0, 4096)
                            if (bytesRead > 0) {
                                output.write(buf, 0, bytesRead)
                            }
                        }
                        val result = output.toByteArray()
                        LocalBroadcastManager.getInstance(this@ThicknessDetectionUploaderService).sendBroadcast(
                            Intent(BROADCAST).apply {
                                putExtra(
                                    BROADCAST_STATUS,
                                    BROADCAST_STATUS_COMPLETED
                                )
                                putExtra(BROADCAST_RESULT_IMAGE, result)
                            }
                        )
                        break
                    }
                    state = stream.next()
                }
            }
        } catch (e: Exception) {
            LocalBroadcastManager.getInstance(this@ThicknessDetectionUploaderService).sendBroadcast(
                Intent(BROADCAST).apply {
                    putExtra(
                        BROADCAST_STATUS,
                        BROADCAST_STATUS_ERROR
                    )
                }
            )
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

        const val BROADCAST = "THICKNESS_DETECTION_SERVICE_BROADCAST"
        const val BROADCAST_STATUS = "BROADCAST_STATUS"
        const val BROADCAST_STATUS_ERROR = "BROADCAST_ERROR"
        const val BROADCAST_STATUS_COMPLETED = "BROADCAST_COMPLETED"

        const val BROADCAST_RESULT_IMAGE = "BROADCAST_RESULT_IMAGE"
    }

}