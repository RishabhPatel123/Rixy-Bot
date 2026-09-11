package com.rixy.bot.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.rixy.bot.BuildConfig

/**
 * Encrypted storage for the Gemini API key and model choice.
 * Nothing here is backed up (allowBackup=false) or readable without this
 * app's Android Keystore master key.
 */
class SecretsStore(context: Context) {

    val defaultModel: String = DEFAULT_MODEL

    var geminiApiKey: String
        get() = prefs.getString(KEY_API_KEY, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var geminiModel: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    /** Agent autonomy: false = supervised (confirm irreversible actions), true = full auto. */
    var agentFullAuto: Boolean
        get() = prefs.getBoolean(KEY_AGENT_FULL_AUTO, false)
        set(value) = prefs.edit().putBoolean(KEY_AGENT_FULL_AUTO, value).apply()

    /** Tools the user marked "always allow" during supervised confirmations. */
    var agentAllowedTools: Set<String>
        get() = prefs.getStringSet(KEY_AGENT_ALLOWED_TOOLS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_AGENT_ALLOWED_TOOLS, value).apply()

    /** Key entered by the user, else the build-time key from .env, else null. */
    fun resolveApiKey(): String? {
        val userKey = geminiApiKey
        if (userKey.isNotEmpty()) return userKey
        val buildKey = BuildConfig.GEMINI_API_KEY
        return if (buildKey.isNotBlank() && buildKey != PLACEHOLDER_KEY) buildKey else null
    }

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    companion object {
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        const val MODEL_OPTIONS =
            "gemini-2.5-flash|gemini-2.5-pro|gemini-2.0-flash|gemini-flash-latest|gemini-3.5-flash"
        private const val PREFS_FILE = "rixy_secrets"
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL = "gemini_model"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_AGENT_FULL_AUTO = "agent_full_auto"
        private const val KEY_AGENT_ALLOWED_TOOLS = "agent_allowed_tools"
        private const val PLACEHOLDER_KEY = "MY_GEMINI_API_KEY"
    }
}
