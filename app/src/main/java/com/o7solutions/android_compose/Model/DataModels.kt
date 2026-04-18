package com.o7solutions.android_compose.Model

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