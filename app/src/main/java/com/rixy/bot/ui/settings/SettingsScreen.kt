package com.rixy.bot.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rixy.bot.BuildConfig
import com.rixy.bot.R
import com.rixy.bot.ui.components.ErrorBanner
import com.rixy.bot.ui.components.PrimaryButton
import com.rixy.bot.ui.components.RixyTextField
import com.rixy.bot.ui.components.SectionHeader
import com.rixy.bot.ui.components.SecondaryButton
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.TextSecondary
import com.rixy.bot.ui.theme.TextTertiary
import com.rixy.bot.ui.viewmodel.SettingsUiState
import com.rixy.bot.ui.viewmodel.SettingsViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val testState by viewModel.testState.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    var keyInput by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffectForChatsDeleted(viewModel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(Modifier.padding(horizontal = Spacing.lg)) {
            SectionHeader(stringResource(R.string.settings_section_api))

            val keySource = when {
                viewModel.hasUserKey -> stringResource(R.string.settings_key_source_user)
                viewModel.hasBuildKey -> stringResource(R.string.settings_key_source_build)
                else -> stringResource(R.string.settings_key_source_none)
            }
            Text(
                keySource,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )

            RixyTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = stringResource(R.string.settings_key_label),
                placeholder = stringResource(R.string.onboarding_key_placeholder),
                secret = true,
            )
            Spacer(Modifier.height(Spacing.md))

            when (val state = testState) {
                is SettingsUiState.Success -> Text(
                    stringResource(R.string.settings_test_success, state.latencyMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = com.rixy.bot.ui.theme.Success,
                )
                is SettingsUiState.Failed -> ErrorBanner(
                    stringResource(R.string.settings_test_failed, state.message)
                )
                is SettingsUiState.Testing -> Text(
                    stringResource(R.string.settings_test) + "…",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                is SettingsUiState.Saved -> Text(
                    stringResource(R.string.settings_save_key) + " ✓",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.rixy.bot.ui.theme.Success,
                )
                SettingsUiState.Idle -> Unit
            }
            Spacer(Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                PrimaryButton(
                    text = stringResource(R.string.settings_save_key),
                    onClick = { viewModel.saveApiKey(keyInput) },
                    enabled = keyInput.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton(
                    text = stringResource(R.string.settings_test),
                    onClick = { viewModel.testApiKey(keyInput) },
                    enabled = keyInput.isNotBlank() || viewModel.hasBuildKey,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            ModelPicker(
                selected = selectedModel,
                options = viewModel.modelOptions,
                onSelect = viewModel::setModel,
            )

            SectionHeader(stringResource(R.string.settings_section_data))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(Spacing.lg)) {
                    Text(
                        stringResource(R.string.settings_delete_all),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        stringResource(R.string.settings_delete_all_confirm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    SecondaryButton(
                        text = stringResource(R.string.settings_delete_all),
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SectionHeader(stringResource(R.string.settings_section_about))
            Text(
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            TextButton(onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RishabhPatel123/Rixy-Bot"))
                    )
                }
            }) {
                Text(stringResource(R.string.settings_source), color = TextSecondary)
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.settings_delete_all)) },
            text = { Text(stringResource(R.string.settings_delete_all_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllChats()
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.chat_delete), color = com.rixy.bot.ui.theme.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.chat_dismiss))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun LaunchedEffectForChatsDeleted(viewModel: SettingsViewModel) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.chatsDeleted.collect { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelPicker(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Text(
        stringResource(R.string.settings_model_label),
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        modifier = Modifier.padding(bottom = Spacing.sm),
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_model_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = MaterialTheme.shapes.medium,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
