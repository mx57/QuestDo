package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_rewards")
data class CustomReward(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val costCoins: Int = 50,
    val iconEmoji: String = "☕",
    val description: String = "",
    val timesClaimed: Int = 0,
    val lastClaimedAt: Long? = null
)
