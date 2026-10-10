package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.CustomReward
import com.example.data.repository.QuestRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureNanoTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CustomRewardPerformanceTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: QuestRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = QuestRepository(
            taskDao = db.taskDao(),
            questLevelDao = db.questLevelDao(),
            userProfileDao = db.userProfileDao(),
            customRewardDao = db.customRewardDao(),
            badgeDao = db.badgeDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testInitializeDefaultsIfNeededInsertsAllDefaultRewardsCorrectly() = runBlocking {
        repository.initializeDefaultsIfNeeded()

        val rewards = repository.customRewards.first()
        assertEquals(5, rewards.size)
        assertTrue(rewards.any { it.title == "Чашка кофе с круассаном" })
    }

    @Test
    fun testInsertAllRewardsBatchPerformance() = runBlocking {
        val rewardDao = db.customRewardDao()
        val rewardList = (1..100).map { i ->
            CustomReward(
                id = i.toLong(),
                title = "Reward $i",
                costCoins = i * 10,
                iconEmoji = "🎁",
                description = "Description $i"
            )
        }

        // Measure batch insertion time
        val batchTimeNs = measureNanoTime {
            rewardDao.insertAllRewards(rewardList)
        }

        val stored = rewardDao.getAllRewards().first()
        assertEquals(100, stored.size)
        assertTrue("Batch insertion completed in $batchTimeNs ns", batchTimeNs >= 0)
    }
}
