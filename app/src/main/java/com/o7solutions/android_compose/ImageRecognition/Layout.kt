package com.o7solutions.android_compose.ImageRecognition

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun AIScanScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var resultText by remember { mutableStateOf("Upload an image to check") }
    var isScanning by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AI Detector", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(20.dp))

        // Image Preview Area
        Card(modifier = Modifier.size(250.dp).padding(8.dp)) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("No Image Selected", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(onClick = {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                selectedImageUri?.let { uri ->
                    isScanning = true
                    val file = uriToFile(context, uri) // Convert URI to File

                    // Pass the state setters so the function can update the UI
                    scanImageForAI(
                        imageFile = file,
                        onResult = { result ->
                            resultText = result
                            isScanning = false
                        },
                        onError = { error ->
                            resultText = "Error: $error"
                            isScanning = false
                        }
                    )                }
            },
            enabled = selectedImageUri != null && !isScanning
        ) {
            if (isScanning) {
                // Use Modifier.size() instead of a size parameter
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scanning...")
            } else {
                Text("Scan for AI")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result Text
        Text(
            text = resultText,
            style = MaterialTheme.typography.bodyLarge,
            color = if (resultText.contains("AI")) Color.Red else Color.DarkGray
        )
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
    tempFile.outputStream().use { output ->
        inputStream?.copyTo(output)
    }
    return tempFile
}

fun scanImageForAI(
    imageFile: File,
    onResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sightengine.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(SightengineApi::class.java)

    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData("media", imageFile.name, requestFile)

    val modelParam = "genai".toRequestBody("text/plain".toMediaTypeOrNull())
    val userParam = "1639974922".toRequestBody("text/plain".toMediaTypeOrNull())
    val secretParam = "HZuuwZxuDKvKB8RMYNMnbCJgmVHhSz78".toRequestBody("text/plain".toMediaTypeOrNull())

    api.checkImage(body, modelParam, userParam, secretParam).enqueue(object : retrofit2.Callback<SightengineResponse> {
        override fun onResponse(call: Call<SightengineResponse>, response: retrofit2.Response<SightengineResponse>) {
            if (response.isSuccessful) {
                val score = response.body()?.type?.ai_generated ?: 0.0
                val percent = (score * 100).toInt()

                if (score > 0.5) {
                    onResult("Likely AI-Generated ($percent%)")
                } else {
                    onResult("Likely Authentic Photo ($percent% AI probability)")
                }
            } else {
                onError("Server error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<SightengineResponse>, t: Throwable) {
            onError(t.message ?: "Unknown Network Error")
        }
    })
}