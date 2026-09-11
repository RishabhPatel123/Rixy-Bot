package com.rixy.bot.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Feeds recent notifications into [NotificationStore] so the Rixy agent can
 * read them and reply to message notifications. The user must grant access
 * once: Settings > Notification access > Rixy.
 */
class RixyNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationStore.listenerConnected = true
        activeNotifications?.forEach(::record)
    }

    override fun onListenerDisconnected() {
        NotificationStore.listenerConnected = false
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let(::record)
    }

    private fun record(sbn: StatusBarNotification) {
        if (sbn.packageName == applicationContext.packageName) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return
        val appLabel = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        }.getOrDefault(sbn.packageName)
        NotificationStore.add(
            NotificationStore.Entry(
                key = sbn.key,
                packageName = sbn.packageName,
                appLabel = appLabel,
                title = title,
                text = text,
                postTime = sbn.postTime,
                actions = sbn.notification.actions ?: emptyArray(),
            )
        )
    }
}
