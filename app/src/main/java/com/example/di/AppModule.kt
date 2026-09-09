package com.example.di

import com.example.data.database.AppDatabase
import com.example.data.repository.CoworkerRepository
import com.example.network.GeminiApiService
import com.example.ui.viewmodel.CoworkerViewModel
import com.example.worker.SyncWorker
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val appModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().coworkerDao() }
    single { CoworkerRepository(get()) }
    single { GeminiApiService(get()) }

    viewModel { CoworkerViewModel(get(), get()) }
    worker { SyncWorker(get(), get(), get()) }
}
