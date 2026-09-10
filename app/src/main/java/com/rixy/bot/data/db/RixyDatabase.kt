package com.rixy.bot.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rixy.bot.data.model.ChatEntity
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.data.model.PlannedTaskEntity

@Database(
    entities = [ChatEntity::class, ChatMessageEntity::class, PlannedTaskEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class RixyDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun taskDao(): TaskDao

    companion object {
        fun build(context: Context): RixyDatabase =
            Room.databaseBuilder(context, RixyDatabase::class.java, "rixy.db")
                .build()
    }
}
