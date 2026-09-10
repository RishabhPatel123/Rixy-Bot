package com.rixy.bot

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.rixy.bot.di.appModule

class RixyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RixyApplication)
            modules(appModule)
        }
    }
}
