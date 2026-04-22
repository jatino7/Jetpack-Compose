package com.o7solutions.android_compose.Model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {


    @GET("categories")
    suspend fun getCategories(): List<Category>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("products")
    suspend fun getProductsByCategory(
        @Query("categoryId") categoryId: Int
    ): List<ProductResponseItem>

    @GET("auth/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): UserResponse

//    @POST("users")
//    suspend fun createUser(@Body user: UserRequest): Response<UserResponse>


//    @POST("auth/login")
//    @FormUrlEncoded
//    suspend fun login(email: String, password: String): Response<LoginResponse>




}