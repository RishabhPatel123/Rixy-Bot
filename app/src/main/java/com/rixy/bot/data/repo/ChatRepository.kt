package com.rixy.bot.data.repo

import androidx.room.withTransaction
import com.rixy.bot.data.db.ChatDao
import com.rixy.bot.data.db.MessageDao
import com.rixy.bot.data.db.RixyDatabase
import com.rixy.bot.data.db.TaskDao
import com.rixy.bot.data.model.ChatEntity
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.data.model.PlannedTaskEntity
import kotlinx.coroutines.flow.Flow

/** Single gateway for chat/message/task persistence. All writes are transactional where multi-table. */
class ChatRepository(
    private val db: RixyDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val taskDao: TaskDao,
) {
    val chats: Flow<List<ChatEntity>> = chatDao.observeChats()
    val tasks: Flow<List<PlannedTaskEntity>> = taskDao.observeTasks()

    fun observeChat(id: Long): Flow<ChatEntity?> = chatDao.observeChatById(id)

    fun observeMessages(chatId: Long): Flow<List<ChatMessageEntity>> =
        messageDao.observeMessagesForChat(chatId)

    suspend fun createChat(title: String, now: Long): Long =
        chatDao.insertChat(ChatEntity(title = title, createdAt = now, lastMessageAt = now))

    suspend fun renameChat(id: Long, title: String) = chatDao.renameChat(id, title)

    suspend fun addMessage(
        chatId: Long,
        isFromUser: Boolean,
        text: String,
        planJson: String? = null,
        imagePath: String? = null,
        sourcesJson: String? = null,
    ) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            messageDao.insertMessage(
                ChatMessageEntity(
                    chatId = chatId,
                    isFromUser = isFromUser,
                    text = text,
                    timestamp = now,
                    planJson = planJson,
                    imagePath = imagePath,
                    sourcesJson = sourcesJson,
                )
            )
            chatDao.touchChat(chatId, now)
        }
    }

    /** Inserts a full imported conversation under one chat. */
    suspend fun importConversation(chatId: Long, messages: List<Pair<Boolean, String>>) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            messages.forEachIndexed { index, (isFromUser, text) ->
                messageDao.insertMessage(
                    ChatMessageEntity(
                        chatId = chatId,
                        isFromUser = isFromUser,
                        text = text,
                        timestamp = now + index,
                    )
                )
            }
            chatDao.touchChat(chatId, now)
        }
    }

    suspend fun getHistory(chatId: Long): List<ChatMessageEntity> =
        messageDao.getMessagesForChat(chatId)

    suspend fun deleteChat(id: Long) {
        db.withTransaction {
            messageDao.deleteMessagesForChat(id)
            taskDao.deleteTasksForChat(id)
            chatDao.deleteChat(id)
        }
    }

    suspend fun deleteAllChats() {
        db.withTransaction {
            messageDao.deleteAllMessages()
            taskDao.deleteAllTasks()
            chatDao.deleteAllChats()
        }
    }

    suspend fun saveTasks(tasks: List<PlannedTaskEntity>) {
        tasks.forEach { taskDao.insertTask(it) }
    }

    suspend fun updateTaskStatus(id: Long, status: String) = taskDao.updateStatus(id, status)

    suspend fun deleteTask(task: PlannedTaskEntity) = taskDao.deleteTask(task)
}
