package com.dbrain.flow.workers

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dbrain.flow.common.*
import com.dbrain.flow.common.ResponseHolder.responses
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.requests.Api
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.util.*

internal abstract class UploadWorker(protected val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    abstract fun apiCall(api: Api, token: String, flowType: FlowType, imageBody: MultipartBody.Part): Response<ResponseBody>
    abstract fun parse(response: String): com.dbrain.flow.models.Response?

    override fun doWork(): Result {
        val file = File(inputData.getString(ARG_FILE))
        val url = inputData.getString(ARG_URL) ?: DEFAULT_URL
        val token = inputData.getString(ARG_TOKEN) ?: return Result.failure(workDataOf(ARG_REASON to "Invalid access token"))
        val params = Gson().fromJson(inputData.getString(ARG_FLOW_TYPE), FlowType::class.java)

        val api = Retrofit.Builder()
            .baseUrl(url)
            .build()
            .create(Api::class.java)

        val requestImageFile = file.asRequestBody(MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.toMediaTypeOrNull())
        val imageBody = MultipartBody.Part.createFormData("image", file.name, requestImageFile)

        val response = apiCall(api, token, params, imageBody)

        val responseBody = response.body()
        val responseError = response.errorBody()

        if (responseBody != null) {
            val p = parse(responseBody.string())
            if (p != null) {
                val id = UUID.randomUUID().toString()
                responses[id] = p
                return Result.success(workDataOf(ARG_RESPONSE to id))
            }
        }
        if (responseError != null) {
            return Result.failure(workDataOf(ARG_RESPONSE to responseError.string()))
        }

        return Result.failure()
    }

}