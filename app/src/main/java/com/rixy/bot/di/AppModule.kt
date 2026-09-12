package com.rixy.bot.di

import com.rixy.bot.data.db.RixyDatabase
import com.rixy.bot.data.prefs.SecretsStore
import com.rixy.bot.data.repo.ChatRepository
import com.rixy.bot.network.GeminiApiService
import com.rixy.bot.ui.viewmodel.ChatViewModel
import com.rixy.bot.ui.viewmodel.SettingsViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val appModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    single { RixyDatabase.build(androidContext()) }
    single { get<RixyDatabase>().chatDao() }
    single { get<RixyDatabase>().messageDao() }
    single { get<RixyDatabase>().taskDao() }
    single { GeminiApiService(get()) }
    single { SecretsStore(androidContext()) }
    single { com.rixy.bot.data.prefs.ImageStore(androidContext()) }
    single { com.rixy.bot.agent.ToolRegistry(com.rixy.bot.agent.ToolFactory.all()) }
    single {
        ChatRepository(
            db = get(),
            chatDao = get(),
            messageDao = get(),
            taskDao = get(),
        )
    }
    // NOTE: never declare `single<Context> { androidContext() }` — androidContext()
    // resolves Context through the registry, so such a definition recurses into
    // itself (StackOverflowError at startup). startKoin { androidContext(...) }
    // already registers it; reference it with androidContext() inside providers.
    viewModel { ChatViewModel(get(), get(), get(), get(), get(), androidContext()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { com.rixy.bot.ui.tasks.TasksViewModel(get()) }
}
