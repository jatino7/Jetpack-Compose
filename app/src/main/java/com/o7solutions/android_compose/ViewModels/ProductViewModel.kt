package com.o7solutions.android_compose.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.o7solutions.android_compose.Model.ProductResponseItem
import com.o7solutions.android_compose.Model.RetrofitClient
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    // State to hold our product list
    var productsByCategory by mutableStateOf<List<ProductResponseItem>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun fetchProductsByCategory(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Replace 'RetrofitInstance' with your actual setup
                val response = RetrofitClient.apiService.getProductsByCategory(id)
                productsByCategory = response
            } catch (e: Exception) {
                // Handle error (e.g., log it or show a toast)
            } finally {
                isLoading = false
            }
        }
    }
}