package com.rixy.bot

import com.rixy.bot.data.model.RetryPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RetryPayloadTest {

    @Test
    fun `round-trips all fields`() {
        val payload = RetryPayload(
            mode = "CHAT",
            prompt = "hello there",
            attachmentPath = "/data/att_1.png",
            webGrounded = true,
        )
        val parsed = RetryPayload.fromJson(payload.toJson())
        assertEquals(payload, parsed)
    }

    @Test
    fun `null attachment stays null`() {
        val payload = RetryPayload("IMAGE", "a cat")
        val parsed = RetryPayload.fromJson(payload.toJson())
        assertEquals("IMAGE", parsed?.mode)
        assertEquals("a cat", parsed?.prompt)
        assertNull(parsed?.attachmentPath)
        assertEquals(false, parsed?.webGrounded)
    }

    @Test
    fun `garbage yields null`() {
        assertNull(RetryPayload.fromJson("not json"))
        assertNull(RetryPayload.fromJson(""))
    }
}
