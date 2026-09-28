package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest_levels")
data class QuestLevel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val levelNumber: Int,
    val title: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isCompleted: Boolean = false,
    val xpEarned: Int = 100,
    val coinsEarned: Int = 25,
    val rewardClaimed: Boolean = false,
    val rewardTitle: String = ""
)
