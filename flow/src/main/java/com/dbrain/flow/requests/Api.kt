package com.dbrain.flow.requests

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface Api {
    @Multipart
    @POST("recognize")
    fun recognize(@Part body: MultipartBody.Part,
                  @Part verifyFields: MultipartBody.Part?,
                  @Query("token") token: String,
                  @Query("doc_type") docType: Array<String>? = null,
                  @Query("mode") mode: String? = null,
                  @Query("with_hitl") withHitl: Boolean? = null,
                  @Query("hitl_async") hitlAsync: Boolean? = null,
                  @Query("hitl_required_fields") hitlRequiredFields: Array<String>? = null,
                  @Query("hitl_sla") hitlSla: String? = null,
                  @Query("gauss") gauss: Int? = null,
                  @Query("quality") quality: Int? = null,
                  @Query("dpi") dpi: Int? = null,
                  @Query("auto_pdf_raw_images") autoPdfRawImages: Boolean? = null,
                  @Query("pdf_raw_images") pdfRawImages: Boolean? = null,
                  @Query("use_internal_api") useInternalApi: Boolean? = null,
                  @Query("check_fake") checkFake: Boolean? = null,
                  @Query("async") async: Boolean? = null,
                  @Query("simple_cropper") simpleCropper: Boolean? = null,
                  @Query("priority") priority: Int? = null,
    ): Call<ResponseBody>
}