package com.o7solutions.android_compose.ViewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.o7solutions.android_compose.Model.LoginRequest
import com.o7solutions.android_compose.Model.RetrofitClient
import com.o7solutions.android_compose.Utils.DataStoreManager
import kotlinx.coroutines.launch

// 1. Extend AndroidViewModel to get access to application context
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // 2. Initialize DataStoreManager using the application context
    private val dataStoreManager = DataStoreManager(application)

    fun login(onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null // Clear previous errors
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()
                    val access = body?.access_token
                    val refresh = body?.refresh_token

                    if (access != null && refresh != null) {
                        // 3. Save tokens to DataStore
                        dataStoreManager.saveTokens(access, refresh)
                        onSuccess(access, refresh)
                    }
                } else {
                    errorMessage = "Invalid Credentials: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}