package com.rixy.bot.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rixy.bot.data.model.ChatEntity
import com.rixy.bot.data.model.ChatMessageEntity
import com.rixy.bot.data.model.PlannedTaskEntity

@Database(
    entities = [ChatEntity::class, ChatMessageEntity::class, PlannedTaskEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class RixyDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun taskDao(): TaskDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN imagePath TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN sourcesJson TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN isError INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN retryJson TEXT")
            }
        }

        fun build(context: Context): RixyDatabase =
            Room.databaseBuilder(context, RixyDatabase::class.java, "rixy.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
