package com.dbrain.recognition.camera

import android.os.Handler
import android.os.Looper
import android.os.Message

import java.util.concurrent.CountDownLatch

class DispatchQueue(threadName: String) : Thread() {

    @Volatile
    var handler: Handler? = null
        private set
    private val syncLatch = CountDownLatch(1)

    init {
        name = threadName
        start()
    }

    fun sendMessage(msg: Message, delay: Int) {
        try {
            syncLatch.await()
            if (delay <= 0) {
                handler!!.sendMessage(msg)
            } else {
                handler!!.sendMessageDelayed(msg, delay.toLong())
            }
        } catch (ignored: Throwable) {
        }

    }

    fun cancelRunnable(runnable: Runnable?) {
        if (runnable == null) return
        try {
            syncLatch.await()
            handler!!.removeCallbacks(runnable)
        } catch (ignored: Throwable) {
        }

    }

    @JvmOverloads
    fun postRunnable(runnable: Runnable?, delay: Long = 0) {
        if (runnable == null) return
        try {
            syncLatch.await()
            if (delay <= 0) {
                handler!!.post(runnable)
            } else {
                handler!!.postDelayed(runnable, delay)
            }
        } catch (ignored: Throwable) {

        }

    }

    fun cleanupQueue() {
        try {
            syncLatch.await()
            handler!!.removeCallbacksAndMessages(null)
        } catch (ignored: Throwable) {
        }

    }

    fun handleMessage(inputMessage: Message) {

    }

    override fun run() {
        Looper.prepare()
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                this@DispatchQueue.handleMessage(msg)
            }
        }
        syncLatch.countDown()
        Looper.loop()
    }
}