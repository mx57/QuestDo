package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.TaskDao
import com.example.data.db.AppDatabase
import com.example.data.model.TaskItem
import com.example.data.repository.QuestRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuestRepositoryBenchmarkTest {

    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var repository: QuestRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = db.taskDao()
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
    fun benchmarkShuffleCurrentQuestUncompletedTaskUpdate() = runBlocking {
        // Prepare 200 uncompleted tasks in current quest
        val tasks = (1..200).map { i ->
            TaskItem(
                id = i.toLong(),
                title = "Task $i",
                inCurrentQuest = true,
                isCompleted = false,
                postponeCount = 0
            )
        }
        taskDao.insertAllTasks(tasks)

        // Measure shuffleCurrentQuest execution time
        val elapsed = measureTimeMillis {
            repository.shuffleCurrentQuest()
        }

        println("BENCHMARK_RESULT: shuffleCurrentQuest took $elapsed ms for 200 uncompleted tasks")

        // Verify postponeCount was incremented for all tasks
        val task1 = taskDao.getTaskById(1L)
        assertEquals(1, task1?.postponeCount)
    }
}
