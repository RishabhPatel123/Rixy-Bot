#!/bin/bash
sed -i '/import javax.inject.Inject/d' app/src/main/java/com/example/network/GeminiApiService.kt
sed -i '/import javax.inject.Singleton/d' app/src/main/java/com/example/network/GeminiApiService.kt
sed -i '/@Singleton/d' app/src/main/java/com/example/network/GeminiApiService.kt
sed -i 's/class GeminiApiService @Inject constructor(private val okHttpClient: OkHttpClient)/class GeminiApiService(private val okHttpClient: OkHttpClient)/g' app/src/main/java/com/example/network/GeminiApiService.kt
