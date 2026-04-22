package com.o7solutions.android_compose.Model

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Int,
    val name: String,
    val image: String
)

data class LoginResponse(
    val access_token: String ?= null,
    val refresh_token: String ?= null
)


data class LoginRequest(
    val email: String ?= null,
    val password: String ?= null
)



data class UserRequest (
    val name: String ?= null,
    val email: String ?= null,
    val password: String ?= null,
    val avatar: String ?= null
)


data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val category: Category,
    val images: List<String>
)

data class UserResponse(
    var id: Int ?= 0,
    var email: String ?= null,
    var password: String ?= null,
    var name: String ?= null,
    var role: String ?= null ,
    var avatar: String ?= null
)

data class ProductResponseItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("description")
    val description: String,
    @SerializedName("images")
    val images: List<String>,
    @SerializedName("category")
    val category: Category
)