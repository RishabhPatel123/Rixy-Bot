package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.CoworkerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val repository: CoworkerRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncWorker", "Starting background sync operation...")
            delay(2000)
            
            val servers = repository.allMcpServers.firstOrNull()
            servers?.forEach { server ->
                if (server.status == "SYNCING") {
                    repository.updateMcpServer(server.copy(status = "CONNECTED"))
                }
            }
            
            Log.d("SyncWorker", "Background sync completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during background sync: ${e.message}")
            Result.retry()
        }
    }
}
