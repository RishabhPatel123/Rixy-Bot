package com.example

import android.app.Application
import androidx.work.Configuration
import com.example.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CoworkerApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(org.koin.androidx.workmanager.factory.KoinWorkerFactory())
            .build()
            
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CoworkerApplication)
            workManagerFactory()
            modules(appModule)
        }
    }
}
