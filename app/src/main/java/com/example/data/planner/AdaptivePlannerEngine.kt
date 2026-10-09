package com.example.data.planner

import com.example.data.model.ScheduleSlot
import com.example.data.model.TaskItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Adaptive AI Planner Engine
 * Implements deterministic priority calculation, time-slot schedule generation,
 * and automatic rescheduling logic as specified in the PathPilot AI architecture.
 *
 * Core Formula:
 * Priority = Urgency + Importance + Career Value + Remaining Work
 */
object AdaptivePlannerEngine {

    /**
     * Calculates deterministic priority score for a given task.
     *
     * Factors:
     * - Urgency (0-50 pts): based on hours remaining until deadline
     * - Importance (5-25 pts): rated 1 to 5
     * - Career Value (5-20 pts): rated 1 to 4
     * - Remaining Work (5-15 pts): based on duration and splittability
     */
    fun calculatePriorityScore(task: TaskItem, currentTimeMs: Long = System.currentTimeMillis()): Double {
        // 1. Urgency calculation
        val diffHours = (task.deadlineTimestamp - currentTimeMs) / (1000.0 * 60 * 60)
        val urgencyScore = when {
            task.status == "Missed" -> 55.0 // Missed tasks get immediate urgent boost for recovery
            diffHours <= 0 -> 50.0          // Overdue
            diffHours <= 12 -> 45.0         // Due within 12 hours
            diffHours <= 24 -> 35.0         // Due tomorrow
            diffHours <= 48 -> 25.0         // Due in 2 days
            diffHours <= 72 -> 18.0         // Due in 3 days
            diffHours <= 168 -> 10.0        // Due within a week
            else -> 5.0                     // Distant deadline
        }

        // 2. Importance calculation (rating 1 to 5)
        // 1=5pts, 2=10pts, 3=15pts, 4=20pts, 5=25pts
        val importanceScore = (task.importance.coerceIn(1, 5) * 5).toDouble()

        // 3. Career Value calculation (rating 1 to 4)
        // 1=5pts, 2=10pts, 3=15pts, 4=20pts
        val careerValueScore = (task.careerValue.coerceIn(1, 4) * 5).toDouble()

        // 4. Remaining Work score
        // Gives slight weight to manageable or splittable chunks that can be accomplished
        val workScore = if (task.canBeSplit) {
            12.0
        } else if (task.estimatedDurationMinutes <= 45) {
            10.0
        } else {
            6.0
        }

        return urgencyScore + importanceScore + careerValueScore + workScore
    }

    /**
     * Generates a deterministic daily schedule based on available study time and task priorities.
     *
     * @param tasks List of active tasks
     * @param totalAvailableMinutes Total study time allocated for today (e.g. 180 min = 3.0 hrs)
     * @param startHour Starting hour of preferred study window (e.g. 9 for 9:00 AM)
     * @param startMinute Starting minute (e.g. 0)
     */
    fun generateSchedule(
        tasks: List<TaskItem>,
        totalAvailableMinutes: Int,
        startHour: Int = 9,
        startMinute: Int = 0
    ): List<ScheduleSlot> {
        val activeTasks = tasks
            .filter { it.status != "Completed" }
            .map { task ->
                val score = calculatePriorityScore(task)
                task.copy(priorityScore = score)
            }
            .sortedByDescending { it.priorityScore }

        val schedule = mutableListOf<ScheduleSlot>()
        var remainingMinutes = totalAvailableMinutes

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

        for (task in activeTasks) {
            if (remainingMinutes <= 0) break

            val taskDuration = max(10, task.estimatedDurationMinutes)

            if (taskDuration <= remainingMinutes) {
                // Task fits completely in today's available time
                val startTimeStr = timeFormat.format(calendar.time)
                calendar.add(Calendar.MINUTE, taskDuration)
                val endTimeStr = timeFormat.format(calendar.time)

                schedule.add(
                    ScheduleSlot(
                        slotId = "slot_${task.id}_full",
                        taskId = task.id,
                        taskName = task.taskName,
                        category = task.subjectCategory,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        durationMinutes = taskDuration,
                        priorityScore = task.priorityScore,
                        isSplitPart = false,
                        isMissed = task.status == "Missed",
                        isCompleted = false,
                        explanationReason = generateReason(task, isFirst = schedule.isEmpty())
                    )
                )

                remainingMinutes -= taskDuration
            } else if (task.canBeSplit && remainingMinutes >= 15) {
                // Task can be split! Allocate remaining available time to Part 1
                val allocatedChunk = remainingMinutes
                val startTimeStr = timeFormat.format(calendar.time)
                calendar.add(Calendar.MINUTE, allocatedChunk)
                val endTimeStr = timeFormat.format(calendar.time)

                schedule.add(
                    ScheduleSlot(
                        slotId = "slot_${task.id}_part1",
                        taskId = task.id,
                        taskName = task.taskName,
                        category = task.subjectCategory,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        durationMinutes = allocatedChunk,
                        priorityScore = task.priorityScore,
                        isSplitPart = true,
                        splitPartInfo = "Part 1 ($allocatedChunk min of ${taskDuration} min)",
                        isMissed = task.status == "Missed",
                        isCompleted = false,
                        explanationReason = "High priority split into today's final available block; Part 2 deferred to tomorrow."
                    )
                )

                remainingMinutes = 0
            }
            // If cannot be split and does not fit, check next smaller tasks in loop
        }

        return schedule
    }

    /**
     * Explains WHY a task is scheduled in this order.
     */
    private fun generateReason(task: TaskItem, isFirst: Boolean): String {
        return when {
            task.status == "Missed" ->
                "Prioritized first because it was previously missed. Rescheduled with high urgency to keep you on track."
            isFirst && task.importance >= 4 ->
                "Scheduled first because of critical deadline and High Academic Importance. Completing this ensures peace of mind."
            task.subjectCategory == "Assignment" && task.priorityScore >= 70 ->
                "Assignment deadline approaching. Prioritized ahead of optional practice to protect your GPA."
            task.subjectCategory == "Exam Prep" ->
                "Exam preparation block positioned while cognitive focus is sharpest."
            task.careerValue >= 3 ->
                "Allocated to advance long-term career portfolio goals alongside required coursework."
            else ->
                "Fitted into available time window based on priority score (${task.priorityScore.toInt()} pts)."
        }
    }
}
