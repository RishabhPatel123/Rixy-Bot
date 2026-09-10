package com.rixy.bot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rixy.bot.data.prefs.SecretsStore
import com.rixy.bot.ui.RixyApp
import com.rixy.bot.ui.theme.RixyTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val secrets: SecretsStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RixyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) {
                    RixyApp(secrets = secrets)
                }
            }
        }
    }
}
