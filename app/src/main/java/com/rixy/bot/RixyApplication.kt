package com.rixy.bot

import android.app.Application
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.rixy.bot.di.appModule
import java.io.File
import java.util.Date

class RixyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()

        // If Koin startup itself ever throws, record it and keep going:
        // MainActivity will surface the report before starting the UI. This
        // prevents a crash-loop in Application from hiding its own report.
        runCatching {
            startKoin {
                androidContext(this@RixyApplication)
                modules(appModule)
            }
        }.onFailure { throwable ->
            writeCrashReport(Thread.currentThread(), throwable)
        }
    }

    private fun installCrashLogger() {
        val systemHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeCrashReport(thread, throwable)
            systemHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashReport(thread: Thread, throwable: Throwable) {
        runCatching {
            File(filesDir, CRASH_FILE).writeText(
                buildString {
                    appendLine("time: ${Date()}")
                    appendLine("thread: $thread")
                    appendLine()
                    appendLine(Log.getStackTraceString(throwable))
                }
            )
        }
    }

    companion object {
        const val CRASH_FILE = "last_crash.txt"

        fun lastCrashReport(context: android.content.Context): String? =
            File(context.filesDir, CRASH_FILE).takeIf { it.exists() }?.readText()
    }
}
