package com.dbrain.flow.workers

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dbrain.flow.common.*
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.requests.Api
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream


internal class UploadWorker(private val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val file = File(inputData.getString(ARG_FILE))
        val token = inputData.getString(ARG_TOKEN) ?: return Result.failure(workDataOf(ARG_REASON to "Invalid access token"))
        val params = Gson().fromJson(inputData.getString(ARG_FLOW_TYPE), FlowType::class.java)
        var verifyFieldsFile: File? = null

        if (params.verifyFields != null) {
            val verifyFields = JSONObject(params.verifyFields).toString()
            verifyFieldsFile = File(appContext.getOutputDirectory(), "verify-fields.json")
            FileOutputStream(verifyFieldsFile).use { stream ->
                stream.write(verifyFields.toByteArray())
            }
        }

        val api = Retrofit.Builder()
            .baseUrl("https://latest.dbrain.io/")
            .build()
            .create(Api::class.java)

        val requestImageFile = file.asRequestBody(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.toMediaTypeOrNull()
        )
        val imageBody = MultipartBody.Part.createFormData("image", file.name, requestImageFile)

        val vfieldsBody = verifyFieldsFile?.run {
            val requestVFieldsFile = asRequestBody(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.toMediaTypeOrNull()
            )
            MultipartBody.Part.createFormData("verify_fields", name, requestVFieldsFile)
        }

        val response = api.recognize(
            imageBody,
            vfieldsBody,
            token,
            params.docType,
            params.mode,
            params.withHitl,
            params.hitlAsync,
            params.hitlRequiredFields,
            params.hitlSla,
            params.gauss,
            params.quality,
            params.dpi,
            params.autoPdfRawImages,
            params.pdfRawImages,
            params.useInternalApi,
            params.checkFake,
            params.async,
            params.simpleCropper,
            params.priority
        ).execute()

        val responseBody = response.body()
        val responseError = response.errorBody()

        if (responseBody != null) {
            return Result.success(workDataOf(ARG_RESPONSE to responseBody.string()))
        }
        if (responseError != null) {
            return Result.failure(workDataOf(ARG_RESPONSE to responseError.string()))
        }

        return Result.failure()
    }

}