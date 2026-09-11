package com.rixy.bot.service

import android.app.Notification
import java.util.ArrayDeque

/**
 * In-memory ring buffer of recent notifications, fed by [RixyNotificationListener].
 * Holds Notification.Action references (with their PendingIntents) so the
 * agent can send replies.
 */
object NotificationStore {
    data class Entry(
        val key: String,
        val packageName: String,
        val appLabel: String,
        val title: String,
        val text: String,
        val postTime: Long,
        val actions: Array<Notification.Action>,
    ) {
        val replyable: Boolean get() = actions.any { it.remoteInputs?.isNotEmpty() == true }
    }

    @Volatile
    var listenerConnected: Boolean = false

    private val entries = ArrayDeque<Entry>()

    @Synchronized
    fun add(entry: Entry) {
        entries.firstOrNull { it.key == entry.key }?.let { entries.remove(it) }
        entries.addFirst(entry)
        while (entries.size > MAX) entries.removeLast()
    }

    @Synchronized
    fun list(): List<Entry> = entries.toList()

    @Synchronized
    fun clear() = entries.clear()

    private const val MAX = 50
}
