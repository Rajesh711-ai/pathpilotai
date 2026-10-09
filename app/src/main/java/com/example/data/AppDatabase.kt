package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ProgressDao
import com.example.data.dao.SkillDao
import com.example.data.dao.StudentDao
import com.example.data.dao.StudentProfileDao
import com.example.data.dao.StudySessionDao
import com.example.data.dao.TaskDao
import com.example.data.model.ProgressRecord
import com.example.data.model.SkillItem
import com.example.data.model.Student
import com.example.data.model.StudentProfile
import com.example.data.model.StudySession
import com.example.data.model.Task
import com.example.data.model.TaskItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentProfile::class,
        Task::class,
        SkillItem::class,
        StudySession::class,
        ProgressRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentProfileDao(): StudentProfileDao
    fun studentDao(): StudentDao = studentProfileDao()
    abstract fun taskDao(): TaskDao
    abstract fun skillDao(): SkillDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pathpilot.db" // SQLite database named pathpilot.db
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val studentDao = database.studentDao()
            val taskDao = database.taskDao()
            val skillDao = database.skillDao()
            val progressDao = database.progressDao()
            val sessionDao = database.studySessionDao()

            // 1. Initial Student Profile
            studentDao.insertOrUpdate(
                Student(
                    id = 1L,
                    name = "Aryan Sharma",
                    courseBranch = "B.Tech Computer Science & Engineering",
                    year = "2nd Year",
                    careerGoal = "Software Engineer",
                    currentSkills = "Python, Git, Basic C++",
                    dailyAvailableHours = 3.0,
                    preferredStudyHours = "Morning (9:00 AM - 12:00 PM)",
                    targetDailyMinutes = 180,
                    bio = "Aspiring Software Engineer passionate about backend systems, algorithms, and AI tooling."
                )
            )

            // Current timestamp helpers
            val now = System.currentTimeMillis()
            val hourMs = 3600_000L
            val dayMs = 86400_000L

            // 2. Initial Sample Tasks demonstrating the exact prompt scenarios:
            // "Task: Complete Python assignment, Deadline: Tomorrow, Importance: Very High, Duration: 2 hours, Career value: High, Can split: Yes"
            taskDao.insertTask(
                TaskItem(
                    taskName = "Complete Python assignment",
                    subjectCategory = "Assignment",
                    deadlineTimestamp = now + (24 * hourMs),
                    deadlineDesc = "Tomorrow 11:59 PM",
                    importance = 4, // Very High
                    estimatedDurationMinutes = 120, // 2 hours
                    careerValue = 3, // High
                    canBeSplit = true,
                    status = "Pending",
                    priorityScore = 85.0,
                    notes = "Data structures and recursion problem set for CSE201."
                )
            )

            taskDao.insertTask(
                TaskItem(
                    taskName = "Operating Systems exam preparation",
                    subjectCategory = "Exam Prep",
                    deadlineTimestamp = now + (48 * hourMs),
                    deadlineDesc = "In 2 days",
                    importance = 5, // Critical
                    estimatedDurationMinutes = 60,
                    careerValue = 3,
                    canBeSplit = true,
                    status = "Pending",
                    priorityScore = 78.0,
                    notes = "Process synchronization, semaphores, and memory management."
                )
            )

            taskDao.insertTask(
                TaskItem(
                    taskName = "Python practice & algorithms",
                    subjectCategory = "Skill Practice",
                    deadlineTimestamp = now + (7 * dayMs),
                    deadlineDesc = "This weekend",
                    importance = 3, // Medium
                    estimatedDurationMinutes = 40,
                    careerValue = 4, // Top Tier career value
                    canBeSplit = true,
                    status = "Pending",
                    priorityScore = 58.0,
                    notes = "LeetCode 75 medium dynamic programming problems."
                )
            )

            taskDao.insertTask(
                TaskItem(
                    taskName = "Technical English practice",
                    subjectCategory = "Routine",
                    deadlineTimestamp = now + (3 * dayMs),
                    deadlineDesc = "In 3 days",
                    importance = 2, // Low
                    estimatedDurationMinutes = 20,
                    careerValue = 2,
                    canBeSplit = false,
                    status = "Pending",
                    priorityScore = 35.0,
                    notes = "Read IEEE technical paper & summary writeup."
                )
            )

            // 3. Initial Career Skills Roadmap for "Software Engineer"
            val skills = listOf(
                SkillItem(
                    name = "Python Programming",
                    category = "Core Language",
                    careerGoal = "Software Engineer",
                    stepOrder = 1,
                    status = "Mastered",
                    progressPercent = 100,
                    description = "Core syntax, OOP, virtual environments, modules"
                ),
                SkillItem(
                    name = "Data Structures & Algorithms",
                    category = "Core CS",
                    careerGoal = "Software Engineer",
                    stepOrder = 2,
                    status = "In Progress",
                    progressPercent = 65,
                    description = "Arrays, linked lists, trees, graphs, sorting, dynamic programming"
                ),
                SkillItem(
                    name = "SQL & Relational Databases",
                    category = "Backend",
                    careerGoal = "Software Engineer",
                    stepOrder = 3,
                    status = "In Progress",
                    progressPercent = 40,
                    description = "Database schema design, queries, joins, indexes, SQLite / PostgreSQL"
                ),
                SkillItem(
                    name = "Git & GitHub Collaboration",
                    category = "Dev Tools",
                    careerGoal = "Software Engineer",
                    stepOrder = 4,
                    status = "Mastered",
                    progressPercent = 90,
                    description = "Branching, PRs, merge conflicts, Git workflow"
                ),
                SkillItem(
                    name = "Web Development Fundamentals",
                    category = "Web",
                    careerGoal = "Software Engineer",
                    stepOrder = 5,
                    status = "In Progress",
                    progressPercent = 50,
                    description = "HTTP protocols, REST APIs, JSON data structures"
                ),
                SkillItem(
                    name = "APIs & Microservices",
                    category = "Backend Architecture",
                    careerGoal = "Software Engineer",
                    stepOrder = 6,
                    status = "Recommended",
                    progressPercent = 15,
                    description = "Building endpoints with Flask/FastAPI, authentication, pagination"
                ),
                SkillItem(
                    name = "AI / ML Basics & LLM APIs",
                    category = "Modern AI",
                    careerGoal = "Software Engineer",
                    stepOrder = 7,
                    status = "In Progress",
                    progressPercent = 35,
                    description = "Prompt engineering, Gemini API integration, embeddings"
                ),
                SkillItem(
                    name = "Full-Stack Portfolio Projects",
                    category = "Practical",
                    careerGoal = "Software Engineer",
                    stepOrder = 8,
                    status = "Recommended",
                    progressPercent = 20,
                    description = "End-to-end applications deployed with live demo"
                ),
                SkillItem(
                    name = "Technical Interview Preparation",
                    category = "Career",
                    careerGoal = "Software Engineer",
                    stepOrder = 9,
                    status = "Recommended",
                    progressPercent = 10,
                    description = "Mock coding rounds, system design basics, behavioral STAR method"
                )
            )
            skillDao.insertSkills(skills)

            // 4. Initial Study Sessions
            sessionDao.insertSession(
                StudySession(
                    taskName = "Python Functions & OOP Lab",
                    category = "Assignment",
                    durationMinutes = 90,
                    timestamp = now - (24 * hourMs),
                    completed = true,
                    notes = "Finished lab 3 ahead of schedule."
                )
            )
            sessionDao.insertSession(
                StudySession(
                    taskName = "DSA Binary Search Practice",
                    category = "Skill Practice",
                    durationMinutes = 45,
                    timestamp = now - (48 * hourMs),
                    completed = true,
                    notes = "Solved 3 medium problems."
                )
            )

            // 5. Initial Weekly Progress History
            progressDao.insertProgress(
                ProgressRecord(
                    dateString = "Mon",
                    completedCount = 4,
                    pendingCount = 1,
                    missedCount = 0,
                    totalMinutesStudied = 150,
                    productivityScore = 92
                )
            )
            progressDao.insertProgress(
                ProgressRecord(
                    dateString = "Tue",
                    completedCount = 3,
                    pendingCount = 2,
                    missedCount = 1,
                    totalMinutesStudied = 120,
                    productivityScore = 78
                )
            )
            progressDao.insertProgress(
                ProgressRecord(
                    dateString = "Wed",
                    completedCount = 5,
                    pendingCount = 0,
                    missedCount = 0,
                    totalMinutesStudied = 180,
                    productivityScore = 96
                )
            )
        }
    }
}
