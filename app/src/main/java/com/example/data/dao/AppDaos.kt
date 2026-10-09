package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProgressRecord
import com.example.data.model.SkillItem
import com.example.data.model.StudentProfile
import com.example.data.model.StudySession
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for StudentProfile entity.
 * Stores and retrieves user profile details locally.
 */
@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM students WHERE id = 1 LIMIT 1")
    fun getStudentProfileFlow(): Flow<StudentProfile?>

    @Query("SELECT * FROM students WHERE id = 1 LIMIT 1")
    suspend fun getStudentProfile(): StudentProfile?

    @Query("SELECT * FROM students WHERE id = 1 LIMIT 1")
    fun getStudentFlow(): Flow<StudentProfile?>

    @Query("SELECT * FROM students WHERE id = 1 LIMIT 1")
    suspend fun getStudent(): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(student: StudentProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProfile(student: StudentProfile)

    @Update
    suspend fun updateStudentProfile(student: StudentProfile)
}

typealias StudentDao = StudentProfileDao

/**
 * Data Access Object (DAO) for Task entity.
 * Stores and retrieves tasks locally with reactive Flow queries and CRUD operations.
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY priorityScore DESC, deadlineTimestamp ASC")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY priorityScore DESC, deadlineTimestamp ASC")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE status != 'Completed' ORDER BY priorityScore DESC, deadlineTimestamp ASC")
    fun getActiveTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status != 'Completed' ORDER BY priorityScore DESC, deadlineTimestamp ASC")
    suspend fun getActiveTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String, completedAt: Long?)

    @Query("UPDATE tasks SET priorityScore = :score WHERE id = :taskId")
    suspend fun updatePriorityScore(taskId: Long, score: Double)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY stepOrder ASC")
    fun getAllSkillsFlow(): Flow<List<SkillItem>>

    @Query("SELECT * FROM skills WHERE careerGoal = :careerGoal ORDER BY stepOrder ASC")
    fun getSkillsForGoalFlow(careerGoal: String): Flow<List<SkillItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillItem>)

    @Update
    suspend fun updateSkill(skill: SkillItem)

    @Query("UPDATE skills SET status = :status, progressPercent = :progress WHERE id = :id")
    suspend fun updateSkillProgress(id: Long, status: String, progress: Int)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Query("SELECT SUM(durationMinutes) FROM study_sessions")
    fun getTotalMinutesStudiedFlow(): Flow<Int?>
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress ORDER BY dateString DESC LIMIT 7")
    fun getRecentProgressFlow(): Flow<List<ProgressRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(record: ProgressRecord)
}
