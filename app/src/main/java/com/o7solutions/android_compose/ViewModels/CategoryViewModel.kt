package com.o7solutions.android_compose.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.o7solutions.android_compose.Model.Category
import com.o7solutions.android_compose.Model.RetrofitClient
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    var categoryListResponse: List<Category> by mutableStateOf(listOf())
    var isLoading: Boolean by mutableStateOf(true)
    var errorMessage: String by mutableStateOf("")

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                // Using the separated client
                val response = RetrofitClient.apiService.getCategories()
                categoryListResponse = response
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "An error occurred"
                isLoading = false
            }
        }
    }
}