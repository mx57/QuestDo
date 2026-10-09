package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.BadgeAchievement
import com.example.data.model.UserProfile
import com.example.data.repository.QuestRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureNanoTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BadgeOptimizationTest {

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
    fun tearDown() {
        db.close()
    }

    @Test
    fun testBatchBadgeUpdateCorrectnessAndPerformance() = runBlocking {
        repository.initializeDefaultsIfNeeded()

        val initialBadges = repository.badges.first()
        val lockedCountInitial = initialBadges.count { !it.isUnlocked }
        assertTrue("Should have locked badges initially", lockedCountInitial > 0)

        // Set up profile conditions that unlock multiple badges simultaneously
        val updatedProfile = UserProfile(
            id = 1,
            totalQuestsCompleted = 10,
            streakDays = 10,
            totalFocusMinutes = 100,
            totalTasksCompleted = 150,
            devilPactSuccesses = 5,
            cauldronSinsBurned = 5
        )
        db.userProfileDao().insertOrUpdateProfile(updatedProfile)

        // Measure batch checkBadges performance via logFocusMinutes
        val elapsedNanos = measureNanoTime {
            repository.logFocusMinutes(10)
        }

        val updatedBadges = repository.badges.first()
        val unlockedBadges = updatedBadges.filter { it.isUnlocked }

        // Assert that multiple badges were unlocked
        assertTrue("At least 5 badges should be unlocked", unlockedBadges.size >= 5)
        val unlockedIds = unlockedBadges.map { it.id }.toSet()
        assertTrue(unlockedIds.contains("FIRST_QUEST"))
        assertTrue(unlockedIds.contains("STREAK_3"))
        assertTrue(unlockedIds.contains("STREAK_7"))
        assertTrue(unlockedIds.contains("FOCUS_50"))
        assertTrue(unlockedIds.contains("CENTURION"))
        assertTrue(unlockedIds.contains("DEMON_PACT_WIN"))
        assertTrue(unlockedIds.contains("CAULDRON_PURGE"))

        System.out.println("Batch badge update time for unlocking ${unlockedBadges.size} badges: ${elapsedNanos / 1_000_000.0} ms")
    }

    @Test
    fun testDirectBatchUpdateInBadgeDao() = runBlocking {
        val badgesToInsert = (1..20).map { i ->
            BadgeAchievement(
                id = "BADGE_$i",
                title = "Badge $i",
                description = "Desc $i",
                iconEmoji = "🏆",
                isUnlocked = false
            )
        }
        db.badgeDao().insertAllBadges(badgesToInsert)

        val toUpdate = badgesToInsert.take(10).map {
            it.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
        }

        val duration = measureNanoTime {
            db.badgeDao().updateBadges(toUpdate)
        }

        val allBadges = db.badgeDao().getAllBadges().first()
        val unlockedCount = allBadges.count { it.isUnlocked }

        assertEquals(10, unlockedCount)
        System.out.println("Batch DAO update duration for 10 badges: ${duration / 1_000_000.0} ms")
    }
}
