package com.dbrain.flow.workers

import android.content.Context
import android.os.Parcelable
import androidx.work.WorkerParameters
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.models.FlowClassifyResponse
import com.dbrain.flow.requests.Api
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

internal class ClassifyWorker(appContext: Context, workerParams: WorkerParameters) : UploadWorker(appContext, workerParams) {

    override fun apiCall(
        api: Api,
        token: String,
        flowType: FlowType,
        imageBody: MultipartBody.Part
    ): Response<ResponseBody> {

        return api.classify(
            imageBody,
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
        return FlowClassifyResponse.parse(JSONObject(response))
    }

}