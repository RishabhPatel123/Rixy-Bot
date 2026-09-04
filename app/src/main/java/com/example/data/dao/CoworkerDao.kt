package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApprovalRequestEntity
import com.example.data.model.BotEntity
import com.example.data.model.McpServerEntity
import com.example.data.model.RoutineEntity
import com.example.data.model.SkillEntity
import com.example.data.model.SwarmEntity
import com.example.data.model.SwarmMessageEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoworkerDao {

    // Bots
    @Query("SELECT * FROM bots ORDER BY name ASC")
    fun getAllBots(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots WHERE id = :id")
    suspend fun getBotById(id: String): BotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBots(bots: List<BotEntity>)

    @Update
    suspend fun updateBot(bot: BotEntity)

    // Swarms
    @Query("SELECT * FROM swarms ORDER BY id DESC")
    fun getAllSwarms(): Flow<List<SwarmEntity>>

    @Query("SELECT * FROM swarms WHERE id = :swarmId")
    fun getSwarmById(swarmId: Long): Flow<SwarmEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwarm(swarm: SwarmEntity): Long

    @Update
    suspend fun updateSwarm(swarm: SwarmEntity)

    // Swarm Messages
    @Query("SELECT * FROM swarm_messages WHERE swarmId = :swarmId ORDER BY timestamp ASC")
    fun getMessagesForSwarm(swarmId: Long): Flow<List<SwarmMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SwarmMessageEntity): Long

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'RUNNING' ORDER BY updatedAt DESC")
    fun getRunningTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    // Approvals (Human-in-the-Loop)
    @Query("SELECT * FROM approvals WHERE isApproved IS NULL ORDER BY timestamp DESC")
    fun getPendingApprovals(): Flow<List<ApprovalRequestEntity>>

    @Query("SELECT * FROM approvals ORDER BY timestamp DESC")
    fun getAllApprovals(): Flow<List<ApprovalRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ApprovalRequestEntity): Long

    @Update
    suspend fun updateApproval(approval: ApprovalRequestEntity)

    // Skills ("Teach a Task")
    @Query("SELECT * FROM skills ORDER BY id DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity): Long

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    // Routines
    @Query("SELECT * FROM routines ORDER BY id DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    // MCP Servers
    @Query("SELECT * FROM mcp_servers ORDER BY id ASC")
    fun getAllMcpServers(): Flow<List<McpServerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMcpServers(servers: List<McpServerEntity>)

    @Update
    suspend fun updateMcpServer(server: McpServerEntity)
}
