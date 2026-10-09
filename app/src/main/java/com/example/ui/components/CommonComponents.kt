package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.VioletTertiary

/**
 * NEXORA TECH Brand Header with project name and tagline
 */
@Composable
fun NexoraBrandHeader(
    student: Student?,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nexora_brand_header"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            IndigoPrimary.copy(alpha = 0.25f),
                            CyanAccent.copy(alpha = 0.15f),
                            VioletTertiary.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndigoPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "NEXORA TECH",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                letterSpacing = 1.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "HACKATHON BUILD",
                                color = CyanAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "PathPilot AI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Your AI-powered guide to better learning and better careers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Profile Avatar Chip
                Surface(
                    shape = CircleShape,
                    color = IndigoPrimary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, IndigoLight),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick() }
                        .testTag("header_profile_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Student Profile",
                            tint = IndigoLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Toast / Alert Banner for Rescheduling and Task Updates
 */
@Composable
fun NotificationBanner(
    message: String,
    isAlert: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("notification_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) RoseDanger.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAlert) RoseDanger.copy(alpha = 0.5f) else EmeraldSuccess.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isAlert) Icons.Default.Warning else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isAlert) RoseDanger else EmeraldSuccess,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Quick Metric Badge
 */
@Composable
fun MetricBadge(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.18f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Comprehensive Dialog to Add a Smart Task
 */
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (
        taskName: String,
        category: String,
        deadlineDesc: String,
        deadlineHours: Int,
        importance: Int,
        durationMinutes: Int,
        careerValue: Int,
        canBeSplit: Boolean,
        notes: String
    ) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Assignment") }
    var deadlineDesc by remember { mutableStateOf("Tomorrow 5:00 PM") }
    var deadlineHours by remember { mutableIntStateOf(24) }
    var importance by remember { mutableIntStateOf(4) } // 1-5
    var durationMinutes by remember { mutableIntStateOf(120) } // 2 hours
    var careerValue by remember { mutableIntStateOf(3) } // 1-4
    var canBeSplit by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Assignment", "Exam Prep", "Skill Practice", "Project", "Routine")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Smart Task",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name *") },
                    placeholder = { Text("e.g. Complete Python assignment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_name_input"),
                    singleLine = true
                )

                // Category selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Deadline selection
                OutlinedTextField(
                    value = deadlineDesc,
                    onValueChange = { deadlineDesc = it },
                    label = { Text("Deadline Description") },
                    placeholder = { Text("e.g. Tomorrow 5:00 PM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Due In: $deadlineHours hours",
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = deadlineHours.toFloat(),
                    onValueChange = { deadlineHours = it.toInt() },
                    valueRange = 2f..168f,
                    steps = 10
                )

                // Importance & Career Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Importance: $importance/5",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = importance.toFloat(),
                            onValueChange = { importance = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Career Value: $careerValue/4",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = careerValue.toFloat(),
                            onValueChange = { careerValue = it.toInt() },
                            valueRange = 1f..4f,
                            steps = 2
                        )
                    }
                }

                // Duration
                Text(
                    text = "Estimated Duration: $durationMinutes min (${durationMinutes / 60}h ${durationMinutes % 60}m)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = durationMinutes.toFloat(),
                    onValueChange = { durationMinutes = it.toInt() },
                    valueRange = 15f..240f,
                    steps = 14
                )

                // Can be split
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Can Be Split Into Sessions",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Adaptive planner can divide across slots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = canBeSplit,
                        onCheckedChange = { canBeSplit = it },
                        modifier = Modifier.testTag("task_can_split_switch")
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Subject code") },
                    placeholder = { Text("e.g. Chapter 4 recursion & dynamic arrays") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskName.isNotBlank()) {
                        onAddTask(
                            taskName.trim(),
                            category,
                            deadlineDesc.trim(),
                            deadlineHours,
                            importance,
                            durationMinutes,
                            careerValue,
                            canBeSplit,
                            notes.trim()
                        )
                        onDismiss()
                    }
                },
                enabled = taskName.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_task_button")
            ) {
                Text("Add to Planner")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Edit Student Profile Dialog
 */
@Composable
fun EditProfileDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    var courseBranch by remember { mutableStateOf(student.courseBranch) }
    var year by remember { mutableStateOf(student.year) }
    var careerGoal by remember { mutableStateOf(student.careerGoal) }
    var currentSkills by remember { mutableStateOf(student.currentSkills) }
    var dailyHours by remember { mutableDoubleStateOf(student.dailyAvailableHours) }
    var preferredHours by remember { mutableStateOf(student.preferredStudyHours) }

    val careerOptions = listOf(
        "Software Engineer",
        "AI/ML Engineer",
        "Full Stack Developer",
        "Cybersecurity Specialist",
        "Cloud & DevOps Engineer"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Student Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = courseBranch,
                    onValueChange = { courseBranch = it },
                    label = { Text("Course / Branch") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Academic Year") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Career Goal",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                careerOptions.forEach { goal ->
                    FilterChip(
                        selected = careerGoal == goal,
                        onClick = { careerGoal = goal },
                        label = { Text(goal, fontSize = 12.sp) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                OutlinedTextField(
                    value = currentSkills,
                    onValueChange = { currentSkills = it },
                    label = { Text("Current Skills (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Daily Available Study Time: ${String.format("%.1f", dailyHours)} hrs",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = dailyHours.toFloat(),
                    onValueChange = { dailyHours = it.toDouble() },
                    valueRange = 1.0f..8.0f,
                    steps = 13
                )

                OutlinedTextField(
                    value = preferredHours,
                    onValueChange = { preferredHours = it },
                    label = { Text("Preferred Study Hours") },
                    placeholder = { Text("e.g. Evening (6:00 PM - 10:00 PM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        student.copy(
                            name = name,
                            courseBranch = courseBranch,
                            year = year,
                            careerGoal = careerGoal,
                            currentSkills = currentSkills,
                            dailyAvailableHours = dailyHours,
                            targetDailyMinutes = (dailyHours * 60).toInt(),
                            preferredStudyHours = preferredHours
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
