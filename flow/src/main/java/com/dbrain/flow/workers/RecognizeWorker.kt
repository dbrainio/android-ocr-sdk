package com.dbrain.flow.workers

import android.content.Context
import android.os.Parcelable
import android.webkit.MimeTypeMap
import androidx.work.WorkerParameters
import com.dbrain.flow.common.getOutputDirectory
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.models.FlowRecognizeResponse
import com.dbrain.flow.requests.Api
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

internal class RecognizeWorker(appContext: Context, workerParams: WorkerParameters) : UploadWorker(appContext, workerParams) {

    override fun apiCall(
        api: Api,
        token: String,
        flowType: FlowType,
        imageBody: MultipartBody.Part
    ): Response<ResponseBody> {
        var verifyFieldsFile: File? = null

        if (flowType.verifyFields != null) {
            val verifyFields = JSONObject(flowType.verifyFields).toString()
            verifyFieldsFile = File(appContext.getOutputDirectory(), "verify-fields.json")
            FileOutputStream(verifyFieldsFile).use { stream ->
                stream.write(verifyFields.toByteArray())
            }
        }

        val vfieldsBody = verifyFieldsFile?.run {
            val requestVFieldsFile = asRequestBody(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.toMediaTypeOrNull()
            )
            MultipartBody.Part.createFormData("verify_fields", name, requestVFieldsFile)
        }

        return api.recognize(
            imageBody,
            vfieldsBody,
            token,
            flowType.docType,
            flowType.mode,
            flowType.withHitl,
            flowType.hitlAsync,
            flowType.hitlRequiredFields,
            flowType.hitlSla,
            flowType.gauss,
            flowType.quality,
            flowType.dpi,
            flowType.autoPdfRawImages,
            flowType.pdfRawImages,
            flowType.useInternalApi,
            flowType.checkFake,
            flowType.async,
            flowType.simpleCropper,
            flowType.priority
        ).execute()
    }

    override fun parse(response: String): com.dbrain.flow.models.Response {
        return FlowRecognizeResponse.parse(JSONObject(response))
    }

}