package com.rixy.bot

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.rixy.bot.di.appModule
import java.io.File

class RixyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
        startKoin {
            androidContext(this@RixyApplication)
            modules(appModule)
        }
    }

    /**
     * Persists the stack trace of any uncaught exception to filesDir/last_crash.txt
     * (kept for the most recent crash only), then delegates to the system handler.
     * Settings → About can surface the file so crashes on devices without adb
     * can still be reported.
     */
    private fun installCrashLogger() {
        val systemHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                File(filesDir, CRASH_FILE).writeText(
                    buildString {
                        appendLine("time: ${java.util.Date()}")
                        appendLine("thread: $thread")
                        appendLine()
                        appendLine(android.util.Log.getStackTraceString(throwable))
                    }
                )
            }
            systemHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        const val CRASH_FILE = "last_crash.txt"

        fun lastCrashReport(context: android.content.Context): String? =
            File(context.filesDir, CRASH_FILE).takeIf { it.exists() }?.readText()
    }
}
