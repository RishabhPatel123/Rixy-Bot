package com.example.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NetworkLog(
    val timestamp: Long,
    val message: String
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

class NetworkMonitorService private constructor() {

    private val _logs = MutableStateFlow<List<NetworkLog>>(emptyList())
    val logs: StateFlow<List<NetworkLog>> = _logs.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val activeRequests = java.util.concurrent.atomic.AtomicInteger(0)

    val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                addLog(message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val statusInterceptor = Interceptor { chain ->
            val count = activeRequests.incrementAndGet()
            _isConnecting.value = count > 0
            
            try {
                chain.proceed(chain.request())
            } finally {
                val remaining = activeRequests.decrementAndGet()
                _isConnecting.value = remaining > 0
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(statusInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun addLog(message: String) {
        val newLog = NetworkLog(System.currentTimeMillis(), message)
        val currentList = _logs.value.toMutableList()
        currentList.add(0, newLog)
        // Keep last 100 logs
        if (currentList.size > 100) {
            currentList.removeLast()
        }
        _logs.value = currentList
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    companion object {
        @Volatile
        private var instance: NetworkMonitorService? = null

        fun getInstance(): NetworkMonitorService {
            return instance ?: synchronized(this) {
                instance ?: NetworkMonitorService().also { instance = it }
            }
        }
    }
}
