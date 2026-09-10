package com.rixy.bot

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class AppNameTest {
    @Test
    fun `app is named Rixy`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals("Rixy", context.getString(R.string.app_name))
    }
}
