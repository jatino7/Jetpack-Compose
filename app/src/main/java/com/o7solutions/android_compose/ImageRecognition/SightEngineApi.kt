package com.o7solutions.android_compose.ImageRecognition

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SightengineApi {
    @Multipart
    @POST("1.0/check.json")
    fun checkImage(
        @Part media: MultipartBody.Part,
        @Part("models") models: RequestBody,
        @Part("api_user") apiUser: RequestBody,
        @Part("api_secret") apiSecret: RequestBody
    ): Call<SightengineResponse>
}