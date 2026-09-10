package com.rixy.bot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A conversation thread. */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val createdAt: Long,
    val lastMessageAt: Long,
)

/** One message in a chat. [planJson], when present, holds a JSON array of planned tasks. */
@Entity(
    tableName = "messages",
    indices = [Index("chatId")],
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chatId: Long,
    @ColumnInfo(name = "isFromUser") val isFromUser: Boolean,
    val text: String,
    val timestamp: Long,
    val planJson: String? = null,
)

/** A task produced by Plan mode and saved by the user. */
@Entity(
    tableName = "planned_tasks",
    indices = [Index("chatId")],
)
data class PlannedTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chatId: Long,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: Long,
) {
    companion object {
        const val STATUS_OPEN = "OPEN"
        const val STATUS_DONE = "DONE"
        const val STATUS_DISMISSED = "DISMISSED"
    }
}
