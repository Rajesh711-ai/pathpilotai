package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleSlot
import com.example.data.model.Student
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.VioletTertiary

@Composable
fun PlannerScreen(
    student: Student?,
    schedule: List<ScheduleSlot>,
    onCompleteTask: (Long) -> Unit,
    onMarkMissed: (Long) -> Unit,
    onFinishEarly: (Long) -> Unit,
    onAdjustTime: (Double) -> Unit,
    onAddNewTask: () -> Unit,
    onExplainSchedule: () -> Unit,
    isNotificationBarPinned: Boolean = true,
    onToggleNotificationBar: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("planner_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Available Time Quick Adjuster & Status Banner
        item {
            AdaptiveControlCard(
                student = student,
                scheduleCount = schedule.size,
                totalMinutes = schedule.sumOf { it.durationMinutes },
                onAdjustTime = onAdjustTime,
                onExplainSchedule = onExplainSchedule
            )
        }

        // 1.5. Hero Feature: Answers "What should I do right now, and why?"
        item {
            WhatShouldIDoRightNowCard(
                currentSlot = schedule.firstOrNull(),
                student = student,
                onComplete = { slot -> onCompleteTask(slot.taskId) },
                onMissed = { slot -> onMarkMissed(slot.taskId) },
                onExplain = onExplainSchedule,
                onAddNewTask = onAddNewTask,
                isNotificationBarPinned = isNotificationBarPinned,
                onToggleNotificationBar = onToggleNotificationBar
            )
        }

        // 2. Schedule Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Adaptive Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Priority = Urgency + Importance + Career Value + Work",
                        style = MaterialTheme.typography.labelSmall,
                        color = IndigoLight
                    )
                }

                Button(
                    onClick = onAddNewTask,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("quick_add_task_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Task", fontSize = 12.sp)
                }
            }
        }

        // 3. If schedule is empty
        if (schedule.isEmpty()) {
            item {
                EmptyScheduleCard(onAddNewTask = onAddNewTask)
            }
        } else {
            // 4. Time Slots Timeline
            itemsIndexed(schedule, key = { _, slot -> slot.slotId }) { index, slot ->
                ScheduleSlotCard(
                    slot = slot,
                    stepNumber = index + 1,
                    isLast = index == schedule.size - 1,
                    onComplete = { onCompleteTask(slot.taskId) },
                    onMissed = { onMarkMissed(slot.taskId) },
                    onFinishEarly = { onFinishEarly(slot.taskId) }
                )
            }
        }

        // 5. Automatic Rescheduling Explainer Card
        item {
            ReschedulingExplainerCard()
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Top control card for adjusting available study time and viewing schedule metrics.
 */
@Composable
fun AdaptiveControlCard(
    student: Student?,
    scheduleCount: Int,
    totalMinutes: Int,
    onAdjustTime: (Double) -> Unit,
    onExplainSchedule: () -> Unit
) {
    val currentHours = student?.dailyAvailableHours ?: 3.0
    val hourOptions = listOf(1.5, 2.0, 3.0, 4.0, 5.0)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("adaptive_control_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanAccent.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Study Budget",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format("%.1f", currentHours)} hrs allocated (${totalMinutes}m planned, $scheduleCount slots)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IndigoPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { onExplainSchedule() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Rationale",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoLight
                        )
                    }
                }
            }

            // Quick hour chips
            Column {
                Text(
                    text = "Quick Adjust Available Time (Triggers Instant Re-Schedule):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hourOptions.forEach { hrs ->
                        val isSelected = (currentHours == hrs)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onAdjustTime(hrs) },
                            label = {
                                Text(
                                    text = "${hrs}h",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("time_chip_${hrs.toInt()}")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Time-Blocked Schedule Slot Card with complete, missed, and early-finish actions.
 */
@Composable
fun ScheduleSlotCard(
    slot: ScheduleSlot,
    stepNumber: Int,
    isLast: Boolean,
    onComplete: () -> Unit,
    onMissed: () -> Unit,
    onFinishEarly: () -> Unit
) {
    val categoryColor = when (slot.category) {
        "Assignment" -> IndigoLight
        "Exam Prep" -> RoseDanger
        "Skill Practice" -> CyanAccent
        "Project" -> VioletTertiary
        else -> AmberWarning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_slot_card_${slot.taskId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isMissed) RoseDanger.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            1.dp,
            if (slot.isMissed) RoseDanger.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Time badge & Category & Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IndigoPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${slot.startTime} – ${slot.endTime}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "${slot.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = slot.category,
                        color = categoryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task Name
            Text(
                text = slot.taskName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Split Badge if task was partitioned
            if (slot.isSplitPart && slot.splitPartInfo != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberWarning.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "⚡ Adaptive Split: ${slot.splitPartInfo}",
                        color = AmberWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Missed alert badge
            if (slot.isMissed) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RoseDanger.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "⚠️ Missed Task: Recalculated with urgent priority boost",
                        color = RoseDanger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // AI Slot Explanation
            if (slot.explanationReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = slot.explanationReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Complete, Missed (Reschedule), Finish Early
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("complete_slot_${slot.taskId}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onFinishEarly,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("finish_early_slot_${slot.taskId}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = CyanAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Finish Early", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onMissed,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("missed_slot_${slot.taskId}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = RoseDanger
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Missed", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleCard(onAddNewTask: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "All Scheduled Tasks Completed!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Great job! Add new tasks or practice your career skills roadmap.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddNewTask,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Add New Task")
            }
        }
    }
}

@Composable
fun ReschedulingExplainerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = IndigoPrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = IndigoLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Automatic Rescheduling Active",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "If you tap 'Missed' or change available study time, PathPilot AI recalculates urgency, shifts lower priorities, and generates a fresh schedule automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Hero Feature: Answers "What should I do right now, and why?"
 * Powered by NEXORA TECH's Adaptive AI Decision Engine
 */
@Composable
fun WhatShouldIDoRightNowCard(
    currentSlot: ScheduleSlot?,
    student: Student?,
    onComplete: (ScheduleSlot) -> Unit,
    onMissed: (ScheduleSlot) -> Unit,
    onExplain: () -> Unit,
    onAddNewTask: () -> Unit,
    isNotificationBarPinned: Boolean = true,
    onToggleNotificationBar: (Boolean) -> Unit = {}
) {
    var isTimerRunning by remember { mutableStateOf(false) }
    val initialSeconds = remember(currentSlot?.slotId) {
        val mins = currentSlot?.durationMinutes ?: 25
        mins * 60
    }
    var secondsRemaining by remember(currentSlot?.slotId) { mutableIntStateOf(initialSeconds) }

    LaunchedEffect(isTimerRunning, secondsRemaining) {
        if (isTimerRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        } else if (secondsRemaining <= 0) {
            isTimerRunning = false
        }
    }

    val progress = if (initialSeconds > 0) {
        ((initialSeconds - secondsRemaining).toFloat() / initialSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val formattedMinutes = secondsRemaining / 60
    val formattedSeconds = secondsRemaining % 60
    val timeString = String.format("%02d:%02d", formattedMinutes, formattedSeconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("what_should_i_do_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    IndigoPrimary.copy(alpha = 0.8f),
                    CyanAccent.copy(alpha = 0.8f),
                    VioletTertiary.copy(alpha = 0.8f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            IndigoPrimary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IndigoPrimary
                    ) {
                        Text(
                            text = "DECISION ENGINE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REAL-TIME PRIORITY",
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { onExplain() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Why this?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                    }
                }
            }

            // The Key Core Question
            Column {
                Text(
                    text = "What should I do right now?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Notification Bar Display Card & Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isNotificationBarPinned)
                    EmeraldSuccess.copy(alpha = 0.12f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isNotificationBarPinned)
                        EmeraldSuccess.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleNotificationBar(!isNotificationBarPinned) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isNotificationBarPinned) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Notification Bar Status",
                            tint = if (isNotificationBarPinned) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isNotificationBarPinned) "Live in Android Notification Bar" else "Display on Notification Bar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNotificationBarPinned) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isNotificationBarPinned) "Pinned to status shade with live task & 'Why this?'" else "Tap switch to pin active focus block to status bar",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isNotificationBarPinned,
                        onCheckedChange = { onToggleNotificationBar(it) },
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("notification_bar_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSuccess
                        )
                    )
                }
            }

            if (currentSlot != null) {
                // Task name & scheduled time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndigoLight.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = currentSlot.category,
                                color = IndigoLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentSlot.startTime} – ${currentSlot.endTime}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = currentSlot.taskName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // The "And Why?" Explanatory Block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(IndigoPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "And Why?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoLight
                            )
                            Text(
                                text = currentSlot.explanationReason.ifBlank { "Top-ranked milestone based on deadline proximity and course priority." },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Interactive Focus Countdown Timer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isTimerRunning) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTimerRunning) "Focus Block in Progress" else "Focus Timer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isTimerRunning) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = timeString,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isTimerRunning) CyanAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanAccent,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isTimerRunning = !isTimerRunning },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("focus_timer_toggle_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerRunning) AmberWarning else IndigoPrimary
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTimerRunning) "Pause Focus" else "Start Focus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isTimerRunning = false
                                secondsRemaining = initialSeconds
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Timer",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = { secondsRemaining += 300 },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("+5m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onComplete(currentSlot) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("right_now_complete_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onMissed(currentSlot) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("right_now_missed_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                        border = BorderStroke(1.dp, RoseDanger.copy(alpha = 0.5f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Missed / Replan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // All caught up state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All Scheduled Tasks Cleared!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Next recommendation: Spend 30–45 mins on ${student?.careerGoal ?: "Software Engineer"} career milestones (e.g. Data Structures & Algorithms, System Design) or log a new assignment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onAddNewTask,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Academic Task", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
