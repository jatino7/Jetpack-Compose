package com.o7solutions.android_compose.ImageRecognition
data class SightengineResponse(
    val status: String,
    val type: TypeData?
)

data class TypeData(
    val ai_generated: Double // Value between 0 and 1
)