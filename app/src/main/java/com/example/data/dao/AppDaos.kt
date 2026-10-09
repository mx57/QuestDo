package com.example.data.dao

import androidx.room.*
import com.example.data.model.BadgeAchievement
import com.example.data.model.CustomReward
import com.example.data.model.QuestLevel
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE inCurrentQuest = 1 AND isCompleted = 0 AND isArchived = 0 ORDER BY priority DESC, id ASC")
    fun getCurrentQuestTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE inCurrentQuest = 0 AND isCompleted = 0 AND isArchived = 0 ORDER BY priority DESC, createdAt DESC")
    fun getBacklogTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isStrictDeadline = 1 AND isCompleted = 0 AND isArchived = 0 ORDER BY dueDate ASC")
    fun getUrgentDeadlineTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 AND createdAt <= :thresholdTimestamp")
    fun getStaleTasks(thresholdTimestamp: Long): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskItem>): List<Long>

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET inCurrentQuest = 0 WHERE inCurrentQuest = 1")
    suspend fun clearCurrentQuest()

    @Query("UPDATE tasks SET inCurrentQuest = 1 WHERE id IN (:taskIds)")
    suspend fun markTasksInCurrentQuest(taskIds: List<Long>)

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND isArchived = 0")
    fun getActiveTasksCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTasksCount(): Flow<Int>
}

@Dao
interface QuestLevelDao {
    @Query("SELECT * FROM quest_levels ORDER BY levelNumber DESC")
    fun getAllLevels(): Flow<List<QuestLevel>>

    @Query("SELECT * FROM quest_levels WHERE isCompleted = 0 ORDER BY id DESC LIMIT 1")
    fun getActiveLevel(): Flow<QuestLevel?>

    @Query("SELECT * FROM quest_levels WHERE isCompleted = 1 ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLastCompletedLevel(): QuestLevel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: QuestLevel): Long

    @Update
    suspend fun updateLevel(level: QuestLevel)

    @Query("SELECT COUNT(*) FROM quest_levels WHERE isCompleted = 1")
    fun getCompletedLevelsCount(): Flow<Int>
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface CustomRewardDao {
    @Query("SELECT * FROM custom_rewards ORDER BY costCoins ASC")
    fun getAllRewards(): Flow<List<CustomReward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: CustomReward): Long

    @Update
    suspend fun updateReward(reward: CustomReward)

    @Delete
    suspend fun deleteReward(reward: CustomReward)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, id ASC")
    fun getAllBadges(): Flow<List<BadgeAchievement>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllBadges(badges: List<BadgeAchievement>)

    @Update
    suspend fun updateBadge(badge: BadgeAchievement)

    @Update
    suspend fun updateBadges(badges: List<BadgeAchievement>)
}
