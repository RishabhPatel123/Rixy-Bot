#!/bin/bash
sed -i '/import androidx.hilt.work.HiltWorker/d' app/src/main/java/com/example/worker/SyncWorker.kt
sed -i '/import dagger.assisted.Assisted/d' app/src/main/java/com/example/worker/SyncWorker.kt
sed -i '/import dagger.assisted.AssistedInject/d' app/src/main/java/com/example/worker/SyncWorker.kt
sed -i '/@HiltWorker/d' app/src/main/java/com/example/worker/SyncWorker.kt
sed -i 's/class SyncWorker @AssistedInject constructor(/class SyncWorker(/g' app/src/main/java/com/example/worker/SyncWorker.kt
sed -i 's/@Assisted appContext: Context,/appContext: Context,/g' app/src/main/java/com/example/worker/SyncWorker.kt
sed -i 's/@Assisted workerParams: WorkerParameters,/workerParams: WorkerParameters,/g' app/src/main/java/com/example/worker/SyncWorker.kt
