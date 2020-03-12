package com.dbrain.recognition.api.internal

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dbrain.recognition.api.Key
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.*

abstract class Request<T>(method: String) {

    interface Callback<T> {
        fun onResponse(response: T)
        fun onError(exception: Exception) { }
        fun onCanceled() { }
    }

    private var multipartFormBuilder: MultipartBody.Builder? = null
    private var isCanceled = false
    private var isExecuting = false
    private var requestTag: String? = null
    private var file: File? = null

    private val uriBuilder = Uri.Builder()
        .scheme(Key.HTTPS)
        .authority(API.domain)
        .appendPath(method)

    fun setDocumentImage(file: File) {
        this.file = file
        checkMultipartFormBuilder()
        multipartFormBuilder!!.addPart(
            Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"img.jpg\""),
            RequestBody.create(MediaType.parse("image/jpeg"), file)
        )
    }

    fun getFile() = file

    fun addQuery(key: String, value: Any) {
        uriBuilder.appendQueryParameter(key, value.toString())
    }

    fun responseUI(callback: Callback<T>) {
        val uiHandler = Handler(Looper.getMainLooper())
        API.executor.submit {
            try {
                val response = response()
                uiHandler.post {
                    callback.onResponse(response)
                }
            } catch (e: Exception) {
                if (e is CancelException) {
                    uiHandler.post {
                        callback.onCanceled()
                    }
                } else {
                    uiHandler.post {
                        callback.onError(e)
                    }
                }
            }
        }
    }

    private fun response(): T {
        isExecuting = true
        Log.d(LOG, "start")
        try {
            val response = execute()
            if (isCanceled) {
                throw CancelException()
            }
            Log.d(LOG, "successfully: $response")
            isExecuting = false
            return response
        } catch (e: Exception) {
            isExecuting = false
            Log.e(LOG, "error", e)
            if (isCanceled) { // for ignore APIException if request canceled
                isCanceled = false
                throw CancelException()
            } else {
                throw e
            }
        }
    }

    private fun execute(): T {
        requestTag = UUID.randomUUID().toString()
        val okHttpRequest = okhttp3.Request.Builder()
            .tag(requestTag)
            .header(Key.AUTHORIZATION, "Token ${API.token}")
            .url(uriBuilder.toString())
        multipartFormBuilder?.let {
            okHttpRequest.post(it.build())
        }

        val okHttpResponse = API.okHttpClient.newCall(okHttpRequest.build()).execute()
        if (!okHttpResponse.isSuccessful) {
            throw APIException("Request is not successful. Code = ${okHttpResponse.code()}")
        }
        val string = okHttpResponse.body()?.string()?: throw APIException(
            "Body is null"
        )
        return parseResponse(JSONObject(string))
    }

    private fun checkMultipartFormBuilder() {
        if (multipartFormBuilder == null) {
            multipartFormBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
    }

    fun cancel() {
        if (isCanceled || !isExecuting) {
            return
        }
        Log.d(LOG, "cancel")
        isCanceled = true
        requestTag?.let {
            API.okHttpClient.dispatcher().queuedCalls().forEach { call ->
                if (call.request().tag() == it) {
                    call.cancel()
                }
            }

            API.okHttpClient.dispatcher().runningCalls().forEach { call ->
                if (call.request().tag() == it) {
                    call.cancel()
                }
            }
        }
    }

    abstract fun parseResponse(json: JSONObject): T

    companion object {
        private const val LOG = "APIRequest"
    }
}