package com.o7solutions.android_compose.Model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {


    @GET("categories")
    suspend fun getCategories(): List<Category>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>


}