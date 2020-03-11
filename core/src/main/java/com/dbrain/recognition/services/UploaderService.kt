package com.dbrain.recognition.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.dbrain.recognition.R
import java.lang.Exception

abstract class UploaderService(private val serviceName: String) : IntentService(serviceName) {

    fun cancel() {
        stopSelf()
    }

    fun start() {
        startForeground(1, getNotificationRendererService(this))
    }

    fun finish() {
        stopForeground(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    fun log(message: String) {
        Log.d(serviceName, message)
    }

    fun loge(exception: Exception) {
        Log.e(serviceName, "", exception)
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
}