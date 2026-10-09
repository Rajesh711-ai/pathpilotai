package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.ProgressRecord
import com.example.data.model.ScheduleSlot
import com.example.data.model.SkillItem
import com.example.data.model.Student
import com.example.data.model.StudySession
import com.example.data.model.TaskItem
import com.example.data.repository.PathPilotRepository
import com.example.notification.PathPilotNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isAlert: Boolean = false
)

class PathPilotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PathPilotRepository

    val studentState: StateFlow<Student?>
    val tasksState: StateFlow<List<TaskItem>>
    val scheduleState: StateFlow<List<ScheduleSlot>>
    val skillsState: StateFlow<List<SkillItem>>
    val sessionsState: StateFlow<List<StudySession>>
    val progressState: StateFlow<List<ProgressRecord>>
    val totalMinutesState: StateFlow<Int?>

    private val _aiGuidanceText = MutableStateFlow<String>("")
    val aiGuidanceText: StateFlow<String> = _aiGuidanceText.asStateFlow()

    private val _isAiLoading = MutableStateFlow<Boolean>(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    private val _selectedTab = MutableStateFlow<Int>(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isNotificationBarEnabled = MutableStateFlow<Boolean>(true)
    val isNotificationBarEnabled: StateFlow<Boolean> = _isNotificationBarEnabled.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PathPilotRepository(
            studentDao = database.studentDao(),
            taskDao = database.taskDao(),
            skillDao = database.skillDao(),
            studySessionDao = database.studySessionDao(),
            progressDao = database.progressDao()
        )

        studentState = repository.studentFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        tasksState = repository.allTasksFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        scheduleState = repository.adaptiveScheduleFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        skillsState = repository.allSkillsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        sessionsState = repository.allSessionsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        progressState = repository.recentProgressFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        totalMinutesState = repository.totalMinutesStudiedFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        fetchAiGuidance()

        // Keep Android notification bar automatically synced with current adaptive top priority
        viewModelScope.launch {
            scheduleState.collect { slots ->
                if (_isNotificationBarEnabled.value && slots.isNotEmpty()) {
                    PathPilotNotificationManager.showPriorityNotification(
                        context = getApplication(),
                        slot = slots.firstOrNull(),
                        student = studentState.value
                    )
                }
            }
        }
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun clearNotification() {
        _notification.value = null
    }

    fun fetchAiGuidance(question: String? = null) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val advice = repository.getAiGuidance(question)
                _aiGuidanceText.value = advice
            } catch (e: Exception) {
                _aiGuidanceText.value = "PathPilot AI: Focus on completing your highest priority assignment first to protect your semester GPA. Schedule dedicated practice slots later."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun markTaskCompleted(taskId: Long) {
        viewModelScope.launch {
            repository.markTaskCompleted(taskId)
            _notification.value = UiNotification(
                message = "Task completed! Study progress logged.",
                isAlert = false
            )
            fetchAiGuidance()
        }
    }

    /**
     * Automatic Rescheduling when student misses a task:
     * Recalculates priorities with urgent penalty, reorganizes time slots,
     * and shows a clear adaptive notification.
     */
    fun markTaskMissed(taskId: Long) {
        viewModelScope.launch {
            repository.markTaskMissedAndReschedule(taskId)
            _notification.value = UiNotification(
                message = "Task missed! Adaptive Planner automatically recalculated priorities & rescheduled.",
                isAlert = true
            )
            fetchAiGuidance("Task was missed, how should I adjust my day?")
        }
    }

    fun finishEarly(taskId: Long, minutesSaved: Int = 30) {
        viewModelScope.launch {
            repository.finishEarly(taskId, minutesSaved)
            _notification.value = UiNotification(
                message = "Great work! You finished early and freed $minutesSaved minutes for skill development.",
                isAlert = false
            )
            fetchAiGuidance()
        }
    }

    fun updateAvailableStudyTime(hours: Double) {
        viewModelScope.launch {
            val current = studentState.value ?: Student()
            val updated = current.copy(
                dailyAvailableHours = hours,
                targetDailyMinutes = (hours * 60).toInt()
            )
            repository.updateStudent(updated)
            _notification.value = UiNotification(
                message = "Available study time updated to $hours hrs. Schedule dynamically re-balanced!",
                isAlert = false
            )
        }
    }

    fun updateStudentProfile(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _notification.value = UiNotification(
                message = "Student Profile updated successfully.",
                isAlert = false
            )
            fetchAiGuidance()
        }
    }

    fun addTask(
        taskName: String,
        category: String,
        deadlineDesc: String,
        deadlineHoursFromNow: Int,
        importance: Int,
        durationMinutes: Int,
        careerValue: Int,
        canBeSplit: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val deadlineMs = System.currentTimeMillis() + (deadlineHoursFromNow * 3600_000L)
            val newTask = TaskItem(
                taskName = taskName,
                subjectCategory = category,
                deadlineTimestamp = deadlineMs,
                deadlineDesc = deadlineDesc,
                importance = importance,
                estimatedDurationMinutes = durationMinutes,
                careerValue = careerValue,
                canBeSplit = canBeSplit,
                status = "Pending",
                notes = notes
            )
            repository.insertTask(newTask)
            _notification.value = UiNotification(
                message = "Task added! Calculated priority and inserted into planner.",
                isAlert = false
            )
            fetchAiGuidance()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            _notification.value = UiNotification(
                message = "Task deleted. Schedule re-optimized.",
                isAlert = false
            )
        }
    }

    fun updateSkillProgress(skillId: Long, status: String, progress: Int) {
        viewModelScope.launch {
            repository.updateSkillProgress(skillId, status, progress)
            _notification.value = UiNotification(
                message = "Skill roadmap milestone updated ($progress%).",
                isAlert = false
            )
        }
    }

    fun addSkillAsStudyTask(skill: SkillItem) {
        viewModelScope.launch {
            val task = TaskItem(
                taskName = "Learn & Practice: ${skill.name}",
                subjectCategory = "Skill Practice",
                deadlineTimestamp = System.currentTimeMillis() + (72 * 3600_000L),
                deadlineDesc = "In 3 days",
                importance = 3,
                estimatedDurationMinutes = 45,
                careerValue = 4,
                canBeSplit = true,
                status = "Pending",
                notes = "Milestone step for career goal: ${skill.careerGoal}"
            )
            repository.insertTask(task)
            _notification.value = UiNotification(
                message = "Added 45m practice task for '${skill.name}' to planner!",
                isAlert = false
            )
            _selectedTab.value = 0 // Jump to Planner to see slot
        }
    }

    fun showNotification(message: String, isAlert: Boolean = false) {
        _notification.value = UiNotification(message = message, isAlert = isAlert)
    }

    fun toggleNotificationBar(enabled: Boolean) {
        _isNotificationBarEnabled.value = enabled
        if (enabled) {
            updateNotificationBar()
            _notification.value = UiNotification(
                message = "Live priority focus displayed on Android notification bar! 🔔",
                isAlert = false
            )
        } else {
            PathPilotNotificationManager.dismissNotification(getApplication())
            _notification.value = UiNotification(
                message = "Notification bar priority hidden.",
                isAlert = false
            )
        }
    }

    fun updateNotificationBar() {
        if (_isNotificationBarEnabled.value) {
            val currentSlot = scheduleState.value.firstOrNull()
            PathPilotNotificationManager.showPriorityNotification(
                context = getApplication(),
                slot = currentSlot,
                student = studentState.value
            )
        }
    }
}
