package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.CoworkerDao
import com.example.data.model.ApprovalRequestEntity
import com.example.data.model.ApprovalType
import com.example.data.model.BotActivityState
import com.example.data.model.BotEntity
import com.example.data.model.BotRole
import com.example.data.model.McpServerEntity
import com.example.data.model.RoutineEntity
import com.example.data.model.SkillEntity
import com.example.data.model.SwarmEntity
import com.example.data.model.SwarmMessageEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromBotRole(value: BotRole): String = value.name

    @TypeConverter
    fun toBotRole(value: String): BotRole = try {
        BotRole.valueOf(value)
    } catch (_: Exception) {
        BotRole.SALES_OUTBOUND
    }

    @TypeConverter
    fun fromBotActivityState(value: BotActivityState): String = value.name

    @TypeConverter
    fun toBotActivityState(value: String): BotActivityState = try {
        BotActivityState.valueOf(value)
    } catch (_: Exception) {
        BotActivityState.IDLE
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (_: Exception) {
        TaskStatus.RUNNING
    }

    @TypeConverter
    fun fromApprovalType(value: ApprovalType): String = value.name

    @TypeConverter
    fun toApprovalType(value: String): ApprovalType = try {
        ApprovalType.valueOf(value)
    } catch (_: Exception) {
        ApprovalType.SEND_EMAIL
    }
}

@Database(
    entities = [
        BotEntity::class,
        SwarmEntity::class,
        SwarmMessageEntity::class,
        TaskEntity::class,
        ApprovalRequestEntity::class,
        SkillEntity::class,
        RoutineEntity::class,
        McpServerEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coworkerDao(): CoworkerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coworker_command_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
