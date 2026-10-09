package com.example

import com.example.data.model.TaskItem
import com.example.data.planner.AdaptivePlannerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptivePlannerEngineTest {

    @Test
    fun testPriorityCalculation_urgentAssignmentTakesTopPriority() {
        val now = System.currentTimeMillis()

        val urgentAssignment = TaskItem(
            id = 1L,
            taskName = "Complete Python assignment",
            subjectCategory = "Assignment",
            deadlineTimestamp = now + (10 * 3600_000L), // Due in 10 hrs (<12h = 45pts)
            importance = 4, // 20pts
            careerValue = 3, // 15pts
            canBeSplit = true, // 12pts
            status = "Pending"
        )

        val optionalPractice = TaskItem(
            id = 2L,
            taskName = "Python practice",
            subjectCategory = "Skill Practice",
            deadlineTimestamp = now + (5 * 86400_000L), // Due in 5 days (10pts)
            importance = 2, // 10pts
            careerValue = 4, // 20pts
            canBeSplit = true, // 12pts
            status = "Pending"
        )

        val score1 = AdaptivePlannerEngine.calculatePriorityScore(urgentAssignment, now)
        val score2 = AdaptivePlannerEngine.calculatePriorityScore(optionalPractice, now)

        assertTrue("Urgent assignment must have higher priority score than distant practice", score1 > score2)
    }

    @Test
    fun testAdaptiveScheduleGeneration_fitsAvailableTime() {
        val now = System.currentTimeMillis()

        val tasks = listOf(
            TaskItem(
                id = 1L,
                taskName = "Assignment",
                subjectCategory = "Assignment",
                deadlineTimestamp = now + (12 * 3600_000L),
                importance = 5,
                estimatedDurationMinutes = 120, // 2 hours
                careerValue = 3,
                canBeSplit = true
            ),
            TaskItem(
                id = 2L,
                taskName = "Exam prep",
                subjectCategory = "Exam Prep",
                deadlineTimestamp = now + (24 * 3600_000L),
                importance = 4,
                estimatedDurationMinutes = 60, // 1 hour
                careerValue = 3,
                canBeSplit = true
            ),
            TaskItem(
                id = 3L,
                taskName = "Optional coding",
                subjectCategory = "Skill Practice",
                deadlineTimestamp = now + (7 * 86400_000L),
                importance = 2,
                estimatedDurationMinutes = 60,
                careerValue = 4,
                canBeSplit = true
            )
        )

        // Given 3 hours = 180 minutes available
        val schedule = AdaptivePlannerEngine.generateSchedule(
            tasks = tasks,
            totalAvailableMinutes = 180,
            startHour = 9,
            startMinute = 0
        )

        assertEquals("Should schedule slots for available 3 hours", 2, schedule.size)
        val totalScheduledMinutes = schedule.sumOf { it.durationMinutes }
        assertEquals(180, totalScheduledMinutes)
    }

    @Test
    fun testMissedTaskBoostsUrgency() {
        val now = System.currentTimeMillis()

        val pendingTask = TaskItem(
            id = 1L,
            taskName = "Lab Work",
            deadlineTimestamp = now + (48 * 3600_000L),
            importance = 3,
            careerValue = 3,
            status = "Pending"
        )

        val missedTask = pendingTask.copy(status = "Missed")

        val pendingScore = AdaptivePlannerEngine.calculatePriorityScore(pendingTask, now)
        val missedScore = AdaptivePlannerEngine.calculatePriorityScore(missedTask, now)

        assertTrue("Missed task must have higher priority than pending counterpart", missedScore > pendingScore)
    }
}
