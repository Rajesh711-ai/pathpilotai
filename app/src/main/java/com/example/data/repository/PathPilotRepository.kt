package com.example.data.repository

import com.example.ai.AiGuidanceEngine
import com.example.data.dao.ProgressDao
import com.example.data.dao.SkillDao
import com.example.data.dao.StudentDao
import com.example.data.dao.StudySessionDao
import com.example.data.dao.TaskDao
import com.example.data.model.ProgressRecord
import com.example.data.model.ScheduleSlot
import com.example.data.model.SkillItem
import com.example.data.model.Student
import com.example.data.model.StudySession
import com.example.data.model.TaskItem
import com.example.data.planner.AdaptivePlannerEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class PathPilotRepository(
    private val studentDao: StudentDao,
    private val taskDao: TaskDao,
    private val skillDao: SkillDao,
    private val studySessionDao: StudySessionDao,
    private val progressDao: ProgressDao
) {
    // Flows
    val studentFlow: Flow<Student?> = studentDao.getStudentFlow()
    val allTasksFlow: Flow<List<TaskItem>> = taskDao.getAllTasksFlow()
    val activeTasksFlow: Flow<List<TaskItem>> = taskDao.getActiveTasksFlow()
    val allSkillsFlow: Flow<List<SkillItem>> = skillDao.getAllSkillsFlow()
    val allSessionsFlow: Flow<List<StudySession>> = studySessionDao.getAllSessionsFlow()
    val recentProgressFlow: Flow<List<ProgressRecord>> = progressDao.getRecentProgressFlow()
    val totalMinutesStudiedFlow: Flow<Int?> = studySessionDao.getTotalMinutesStudiedFlow()

    /**
     * Dynamically generated Adaptive Schedule combining current student available time and active tasks.
     */
    val adaptiveScheduleFlow: Flow<List<ScheduleSlot>> = combine(studentFlow, allTasksFlow) { student, tasks ->
        val availableMinutes = ((student?.dailyAvailableHours ?: 3.0) * 60).toInt()
        val parsedStartHour = parseStartHour(student?.preferredStudyHours)
        AdaptivePlannerEngine.generateSchedule(
            tasks = tasks,
            totalAvailableMinutes = availableMinutes,
            startHour = parsedStartHour
        )
    }

    private fun parseStartHour(preferredHours: String?): Int {
        if (preferredHours == null) return 9
        return when {
            preferredHours.contains("Morning", ignoreCase = true) -> 9
            preferredHours.contains("Afternoon", ignoreCase = true) -> 14
            preferredHours.contains("Evening", ignoreCase = true) -> 18
            preferredHours.contains("Night", ignoreCase = true) -> 21
            else -> 9
        }
    }

    // Student profile operations
    suspend fun updateStudent(student: Student) {
        studentDao.insertOrUpdate(student)
        recalculateAndSavePriorities()
    }

    // Task operations
    suspend fun insertTask(task: TaskItem): Long {
        val score = AdaptivePlannerEngine.calculatePriorityScore(task)
        val id = taskDao.insertTask(task.copy(priorityScore = score))
        recalculateAndSavePriorities()
        return id
    }

    suspend fun updateTask(task: TaskItem) {
        val score = AdaptivePlannerEngine.calculatePriorityScore(task)
        taskDao.updateTask(task.copy(priorityScore = score))
        recalculateAndSavePriorities()
    }

    suspend fun deleteTask(id: Long) {
        taskDao.deleteTaskById(id)
        recalculateAndSavePriorities()
    }

    /**
     * Marks a task as completed and records a study session.
     */
    suspend fun markTaskCompleted(taskId: Long) {
        val task = taskDao.getTaskById(taskId)
        taskDao.updateTaskStatus(taskId, "Completed", System.currentTimeMillis())
        if (task != null) {
            studySessionDao.insertSession(
                StudySession(
                    taskId = taskId,
                    taskName = task.taskName,
                    category = task.subjectCategory,
                    durationMinutes = task.estimatedDurationMinutes,
                    completed = true
                )
            )
        }
        recalculateAndSavePriorities()
    }

    /**
     * Automatic Rescheduling when a task is missed:
     * 1. Mark task status as "Missed"
     * 2. Recalculate its urgency and priority score
     * 3. Recalculate all other task priorities
     * 4. Regenerate schedule
     */
    suspend fun markTaskMissedAndReschedule(taskId: Long) {
        taskDao.updateTaskStatus(taskId, "Missed", null)
        recalculateAndSavePriorities()
    }

    /**
     * Finish Early action: adjusts remaining task duration and logs study time.
     */
    suspend fun finishEarly(taskId: Long, minutesSaved: Int = 30) {
        val task = taskDao.getTaskById(taskId)
        if (task != null) {
            val actualMinutes = (task.estimatedDurationMinutes - minutesSaved).coerceAtLeast(15)
            studySessionDao.insertSession(
                StudySession(
                    taskId = taskId,
                    taskName = task.taskName,
                    category = task.subjectCategory,
                    durationMinutes = actualMinutes,
                    completed = true,
                    notes = "Finished early! Saved $minutesSaved minutes."
                )
            )
            taskDao.updateTaskStatus(taskId, "Completed", System.currentTimeMillis())
            recalculateAndSavePriorities()
        }
    }

    /**
     * Recalculates deterministic priority scores for all active tasks.
     */
    suspend fun recalculateAndSavePriorities() {
        val active = taskDao.getActiveTasks()
        for (task in active) {
            val score = AdaptivePlannerEngine.calculatePriorityScore(task)
            taskDao.updatePriorityScore(task.id, score)
        }
    }

    // Skills operations
    suspend fun updateSkillProgress(skillId: Long, status: String, progress: Int) {
        skillDao.updateSkillProgress(skillId, status, progress)
    }

    // AI Guidance
    suspend fun getAiGuidance(userQuestion: String? = null): String {
        val student = studentDao.getStudent() ?: Student()
        val tasks = taskDao.getActiveTasks()
        val skills = skillDao.getAllSkillsFlow().firstOrNull() ?: emptyList()
        return AiGuidanceEngine.getPersonalizedGuidance(student, tasks, skills, userQuestion)
    }
}
