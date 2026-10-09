package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.model.StudySession
import com.example.data.planner.AdaptivePlannerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_DONE = "com.example.pathpilot.ACTION_MARK_DONE"
        const val ACTION_REPLAN = "com.example.pathpilot.ACTION_REPLAN"
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context, scope)

                if (action == ACTION_MARK_DONE && taskId != -1L) {
                    val task = db.taskDao().getTaskById(taskId)
                    db.taskDao().updateTaskStatus(taskId, "Completed", System.currentTimeMillis())

                    if (task != null) {
                        db.studySessionDao().insertSession(
                            StudySession(
                                taskId = taskId,
                                taskName = task.taskName,
                                category = task.subjectCategory,
                                durationMinutes = task.estimatedDurationMinutes.coerceAtLeast(15),
                                timestamp = System.currentTimeMillis(),
                                notes = "Completed via Notification Quick Action"
                            )
                        )
                    }
                }

                // Recalculate schedule and refresh the notification bar
                val student = db.studentProfileDao().getStudentProfile()
                val activeTasks = db.taskDao().getActiveTasks()
                val availableMinutes = ((student?.dailyAvailableHours ?: 3.5) * 60).toInt()
                val schedule = AdaptivePlannerEngine.generateSchedule(
                    tasks = activeTasks,
                    totalAvailableMinutes = availableMinutes,
                    startHour = 9
                )

                PathPilotNotificationManager.showPriorityNotification(
                    context = context,
                    slot = schedule.firstOrNull(),
                    student = student
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
