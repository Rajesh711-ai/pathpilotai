package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.dao.StudentProfileDao
import com.example.data.dao.TaskDao
import com.example.data.model.StudentProfile
import com.example.data.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseEntitiesTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var studentProfileDao: StudentProfileDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = database.taskDao()
        studentProfileDao = database.studentProfileDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testStudentProfileDao_insertAndRetrieve() = runBlocking {
        val profile = StudentProfile(
            id = 1L,
            name = "Test Student",
            courseBranch = "Computer Science",
            year = "3rd Year",
            careerGoal = "AI / ML Engineer",
            currentSkills = "Kotlin, Python, PyTorch",
            dailyAvailableHours = 4.0,
            preferredStudyHours = "Morning (8:00 AM - 12:00 PM)",
            targetDailyMinutes = 240,
            bio = "Passionate about Machine Learning."
        )

        studentProfileDao.insertStudentProfile(profile)

        val retrieved = studentProfileDao.getStudentProfile()
        assertNotNull(retrieved)
        assertEquals("Test Student", retrieved?.name)
        assertEquals("AI / ML Engineer", retrieved?.careerGoal)
        assertEquals(4.0, retrieved?.dailyAvailableHours ?: 0.0, 0.01)

        // Verify Reactive Flow
        val flowValue = studentProfileDao.getStudentProfileFlow().first()
        assertNotNull(flowValue)
        assertEquals("Test Student", flowValue?.name)
    }

    @Test
    fun testStudentProfileDao_update() = runBlocking {
        val profile = StudentProfile(
            id = 1L,
            name = "Alex Chen",
            courseBranch = "CSE",
            careerGoal = "Full Stack Engineer"
        )
        studentProfileDao.insertStudentProfile(profile)

        val updated = profile.copy(careerGoal = "Software Engineer", dailyAvailableHours = 5.0)
        studentProfileDao.updateStudentProfile(updated)

        val retrieved = studentProfileDao.getStudentProfile()
        assertEquals("Software Engineer", retrieved?.careerGoal)
        assertEquals(5.0, retrieved?.dailyAvailableHours ?: 0.0, 0.01)
    }

    @Test
    fun testTaskDao_insertQueryUpdateDelete() = runBlocking {
        val task1 = Task(
            id = 100L,
            taskName = "Distributed Systems Assignment",
            subjectCategory = "Assignment",
            deadlineTimestamp = System.currentTimeMillis() + 86400000L,
            deadlineDesc = "Tomorrow 5:00 PM",
            importance = 5,
            estimatedDurationMinutes = 120,
            careerValue = 3,
            canBeSplit = true,
            status = "Pending",
            priorityScore = 95.0,
            notes = "Include raft consensus proofs"
        )

        val task2 = Task(
            id = 101L,
            taskName = "LeetCode Mediums: Graph Traversal",
            subjectCategory = "Skill Practice",
            deadlineTimestamp = System.currentTimeMillis() + 172800000L,
            deadlineDesc = "In 2 days",
            importance = 4,
            estimatedDurationMinutes = 60,
            careerValue = 4,
            canBeSplit = false,
            status = "Pending",
            priorityScore = 80.0
        )

        taskDao.insertTasks(listOf(task1, task2))

        val allTasks = taskDao.getAllTasks()
        assertEquals(2, allTasks.size)

        // Query by ID
        val singleTask = taskDao.getTaskById(100L)
        assertNotNull(singleTask)
        assertEquals("Distributed Systems Assignment", singleTask?.taskName)
        assertEquals(5, singleTask?.importance)

        // Update status
        taskDao.updateTaskStatus(100L, "Completed", System.currentTimeMillis())
        val updatedTask = taskDao.getTaskById(100L)
        assertEquals("Completed", updatedTask?.status)
        assertNotNull(updatedTask?.completedAt)

        // Active tasks flow should now only have task 101
        val activeTasks = taskDao.getActiveTasksFlow().first()
        assertEquals(1, activeTasks.size)
        assertEquals(101L, activeTasks.first().id)

        // Delete task
        taskDao.deleteTaskById(101L)
        val remainingAfterDelete = taskDao.getAllTasks()
        assertEquals(1, remainingAfterDelete.size)

        // Clear all
        taskDao.clearAll()
        val emptyList = taskDao.getAllTasks()
        assertTrue(emptyList.isEmpty())
    }
}
