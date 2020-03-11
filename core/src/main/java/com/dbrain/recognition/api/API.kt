package com.dbrain.recognition.api

import com.dbrain.recognition.data.ClassifiedItem
import com.dbrain.recognition.data.RecognizedItem
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException

object API {
    private const val recognize = "recognize"
    private const val classify = "classify"
    private const val domain = "https://v3-3-4.dbrain.io/"
    private const val token = "UQYkXD0yc72ZV72AoDNOecZBKq2z6LmMJhHQotDhNHMXP6RdN1HQZovqpy7WGye8"

    private val okHttpClient = OkHttpClient()

    fun recognize(type: String, fileName: String): ArrayList<RecognizedItem> {
        val body = request(createRequestBuilder(
            "${getUrl(recognize)}?${Key.DOC_TYPE}=$type"
        ).post(createMultipartBody(fileName)).build())
        val array = body.optJSONArray(Key.ITEMS)?: throw APIException("Not found items")
        return RecognizedItem.list(array)
    }

    fun classify(fileName: String): ArrayList<ClassifiedItem> {
        val body = request(createRequestBuilder(
            getUrl(classify)
        ).post(createMultipartBody(fileName)).build())
        val array = body.optJSONArray(Key.ITEMS)?: throw APIException("Not found items")
        return ClassifiedItem.list(array)
    }

    private fun request(request: Request): JSONObject {
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw APIException("Request is not successful. Code = ${response.code()}")
        }
        val body = response.body()?.string()?: throw APIException("Body is null")
        return JSONObject(body)
    }

    private fun getUrl(method: String) = domain + method

    private fun createMultipartBody(fileName: String): MultipartBody {
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(
                Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"img.jpg\""),
                RequestBody.create(MediaType.parse("image/jpeg"), File(fileName))
            )
            .build()
    }

    private fun createRequestBuilder(url: String): Request.Builder {
        return Request.Builder()
            .header("Authorization", "Token $token")
            .url(url)
    }

    private class APIException(override val message: String) : IOException(message)
}