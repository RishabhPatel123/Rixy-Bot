package com.rixy.bot.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rixy.bot.data.model.ChatEntity
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.data.model.PlannedTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastMessageAt DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id")
    fun observeChatById(id: Long): Flow<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET title = :title WHERE id = :id")
    suspend fun renameChat(id: Long, title: String)

    @Query("UPDATE chats SET lastMessageAt = :timestamp WHERE id = :id")
    suspend fun touchChat(id: Long, timestamp: Long)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChat(id: Long)

    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC, id ASC")
    fun observeMessagesForChat(chatId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC, id ASC")
    suspend fun getMessagesForChat(chatId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM planned_tasks ORDER BY createdAt DESC")
    fun observeTasks(): Flow<List<PlannedTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannedTaskEntity): Long

    @Query("UPDATE planned_tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Delete
    suspend fun deleteTask(task: PlannedTaskEntity)

    @Query("DELETE FROM planned_tasks WHERE chatId = :chatId")
    suspend fun deleteTasksForChat(chatId: Long)

    @Query("DELETE FROM planned_tasks")
    suspend fun deleteAllTasks()
}
