package com.rixy.bot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rixy.bot.data.prefs.SecretsStore
import com.rixy.bot.data.repo.ChatRepository
import com.rixy.bot.network.GeminiApiService
import com.rixy.bot.network.GeminiException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Idle : SettingsUiState
    data class Testing(val key: String) : SettingsUiState
    data class Success(val latencyMs: Long) : SettingsUiState
    data class Failed(val message: String) : SettingsUiState
    data object Saved : SettingsUiState
}

class SettingsViewModel(
    private val secrets: SecretsStore,
    private val gemini: GeminiApiService,
    private val repository: ChatRepository,
) : ViewModel() {

    val defaultModel: String = secrets.defaultModel
    val modelOptions: List<String> = SecretsStore.MODEL_OPTIONS.split("|")

    val hasUserKey: Boolean get() = secrets.geminiApiKey.isNotEmpty()
    val hasBuildKey: Boolean get() = secrets.resolveApiKey() != null && !hasUserKey

    private val _selectedModel = MutableStateFlow(secrets.geminiModel)
    val selectedModel = _selectedModel.asStateFlow()

    private val _testState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val testState = _testState.asStateFlow()

    private val _chatsDeleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val chatsDeleted = _chatsDeleted.asSharedFlow()

    fun setModel(model: String) {
        secrets.geminiModel = model
        _selectedModel.value = model
    }

    fun saveApiKey(key: String, onDone: () -> Unit = {}) {
        secrets.geminiApiKey = key
        _testState.value = SettingsUiState.Saved
        onDone()
    }

    fun testApiKey(key: String? = null) {
        val effectiveKey = key?.trim().orEmpty().ifEmpty { secrets.resolveApiKey() ?: return }
        _testState.value = SettingsUiState.Testing(effectiveKey)
        viewModelScope.launch {
            try {
                val latency = gemini.testApiKey(effectiveKey, secrets.geminiModel)
                _testState.value = SettingsUiState.Success(latency)
            } catch (e: Exception) {
                _testState.value = SettingsUiState.Failed(failureMessage(e))
            }
        }
    }

    fun completeOnboarding() {
        secrets.onboardingDone = true
    }

    // ---- Agent ----

    val agentFullAuto: Boolean get() = secrets.agentFullAuto

    fun setAgentFullAuto(enabled: Boolean) {
        secrets.agentFullAuto = enabled
    }

    val agentAllowedToolCount: Int get() = secrets.agentAllowedTools.size

    fun resetAgentAllowedTools() {
        secrets.agentAllowedTools = emptySet()
    }

    fun deleteAllChats() {
        viewModelScope.launch {
            runCatching { repository.deleteAllChats() }
            _chatsDeleted.tryEmit(Unit)
        }
    }

    private fun failureMessage(e: Exception): String = when (e) {
        is GeminiException.Auth -> "API key was rejected"
        is GeminiException.ModelNotFound -> "Model not found for this key"
        is GeminiException.RateLimited -> "Rate limited — try again shortly"
        is GeminiException.Network -> "Network unreachable"
        else -> "Request failed"
    }
}
