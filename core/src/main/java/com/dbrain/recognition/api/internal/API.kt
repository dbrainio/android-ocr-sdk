package com.dbrain.recognition.api.internal

import okhttp3.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object API {
    const val domain = "v3-3-4.dbrain.io"
    const val token = "UQYkXD0yc72ZV72AoDNOecZBKq2z6LmMJhHQotDhNHMXP6RdN1HQZovqpy7WGye8"
    val okHttpClient = OkHttpClient.Builder()
        .callTimeout(25, TimeUnit.SECONDS)
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    val executor = Executors.newCachedThreadPool()
}