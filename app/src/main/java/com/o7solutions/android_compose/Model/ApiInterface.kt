package com.o7solutions.android_compose.Model

import retrofit2.http.GET

interface ApiService {


    @GET("categories")
    suspend fun getCategories(): List<Category>


}