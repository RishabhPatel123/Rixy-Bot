package com.rixy.bot.ui.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rixy.bot.R
import com.rixy.bot.ui.components.ErrorBanner
import com.rixy.bot.ui.components.PrimaryButton
import com.rixy.bot.ui.components.RixyTextField
import com.rixy.bot.ui.components.SecondaryButton
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.TextSecondary
import com.rixy.bot.ui.viewmodel.SettingsUiState
import com.rixy.bot.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val key by viewModel.testState.collectAsStateWithLifecycle()
    var keyInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.huge))

        Text(
            stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.lg))

        RixyTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            label = stringResource(R.string.onboarding_key_label),
            placeholder = stringResource(R.string.onboarding_key_placeholder),
            secret = true,
        )
        Spacer(Modifier.height(Spacing.md))

        when (val state = key) {
            is SettingsUiState.Failed -> ErrorBanner(
                stringResource(R.string.settings_test_failed, state.message),
                modifier = Modifier.fillMaxWidth(),
            )
            is SettingsUiState.Testing -> Text(
                stringResource(R.string.onboarding_test_key) + "…",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
            else -> Unit
        }
        Spacer(Modifier.height(Spacing.lg))

        PrimaryButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = {
                viewModel.saveApiKey(keyInput)
                viewModel.completeOnboarding()
                onFinished()
            },
            enabled = keyInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.md))
        SecondaryButton(
            text = stringResource(R.string.onboarding_test_key),
            onClick = { viewModel.testApiKey(keyInput) },
            enabled = keyInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.md))
        TextButton(onClick = {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                )
            }
        }) {
            Text(stringResource(R.string.onboarding_get_key), color = TextSecondary)
        }
        Spacer(Modifier.height(Spacing.sm))
        TextButton(onClick = {
            viewModel.completeOnboarding()
            onFinished()
        }) {
            Text(stringResource(R.string.onboarding_skip), color = TextSecondary)
        }
    }
}
