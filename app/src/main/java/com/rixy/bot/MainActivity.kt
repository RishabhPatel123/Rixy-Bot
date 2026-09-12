package com.rixy.bot

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rixy.bot.data.prefs.SecretsStore
import com.rixy.bot.ui.RixyApp
import com.rixy.bot.ui.theme.RixyTheme
import org.koin.android.ext.android.inject
import java.io.File

class MainActivity : ComponentActivity() {

    private val secrets: SecretsStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val lastCrash = RixyApplication.lastCrashReport(this)
        if (lastCrash != null && savedInstanceState == null) {
            showCrashReportBeforeUi(lastCrash)
        } else {
            startAppUi()
        }
    }

    private fun startAppUi() {
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

    /**
     * Renders the previous crash with zero Compose involvement, BEFORE any UI
     * code that may itself be the crasher. The app only starts after the user
     * dismisses the report, so even a 100%-reproducible startup crash can be
     * captured and shared without adb.
     */
    private fun showCrashReportBeforeUi(report: String) {
        val padding = (16 * resources.displayMetrics.density).toInt()
        val scroll = ScrollView(this).apply {
            setPadding(padding, padding, padding, 0)
            addView(
                TextView(this@MainActivity).apply {
                    text = report.take(4000)
                    textSize = 12f
                    setTypeface(Typeface.MONOSPACE)
                    setTextIsSelectable(true)
                }
            )
        }
        val title = TextView(this).apply {
            text = getString(R.string.crash_dialog_title)
            setPadding(padding, padding, padding, 0)
            textSize = 18f
            setTypeface(Typeface.DEFAULT_BOLD)
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(title)
            addView(
                scroll,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
                )
            )
        }
        AlertDialog.Builder(this)
            .setView(root)
            .setPositiveButton(android.R.string.copy) { _, _ ->
                runCatching {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("rixy_crash", report))
                }
                startAppUi()
            }
            .setNegativeButton(R.string.crash_dialog_clear) { _, _ ->
                File(filesDir, RixyApplication.CRASH_FILE).delete()
                startAppUi()
            }
            .setOnCancelListener { startAppUi() }
            .show()
    }
}
