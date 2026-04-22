package com.o7solutions.android_compose.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.o7solutions.android_compose.Model.RetrofitClient
import com.o7solutions.android_compose.Model.UserResponse
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var userState by mutableStateOf<UserResponse?>(null)
    var isLoading by mutableStateOf(false)

    fun fetchProfile(token: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getUserProfile("Bearer $token")
                userState = response
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }
}