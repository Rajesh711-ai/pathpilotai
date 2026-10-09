package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.NexoraBrandHeader
import com.example.ui.components.NotificationBanner
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.ArchitectureScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PathPilotViewModel

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

class MainActivity : ComponentActivity() {

    private val viewModel: PathPilotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PathPilotApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PathPilotApp(viewModel: PathPilotViewModel) {
    val student by viewModel.studentState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasksState.collectAsStateWithLifecycle()
    val schedule by viewModel.scheduleState.collectAsStateWithLifecycle()
    val skills by viewModel.skillsState.collectAsStateWithLifecycle()
    val sessions by viewModel.sessionsState.collectAsStateWithLifecycle()
    val progress by viewModel.progressState.collectAsStateWithLifecycle()
    val totalMinutes by viewModel.totalMinutesState.collectAsStateWithLifecycle()
    val aiGuidanceText by viewModel.aiGuidanceText.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isNotificationBarEnabled by viewModel.isNotificationBarEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleNotificationBar(true)
        } else {
            viewModel.showNotification("Notification permission not granted. You can enable it in device settings to pin priority tasks.", isAlert = true)
        }
    }

    val handleToggleNotificationBar: (Boolean) -> Unit = { shouldEnable ->
        if (shouldEnable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    viewModel.toggleNotificationBar(true)
                } else {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                viewModel.toggleNotificationBar(true)
            }
        } else {
            viewModel.toggleNotificationBar(false)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.updateNotificationBar()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.updateNotificationBar()
        }
    }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavItem("Planner", Icons.Default.DateRange, 0),
        NavItem("Tasks", Icons.Default.CheckCircle, 1),
        NavItem("Roadmap", Icons.AutoMirrored.Filled.TrendingUp, 2),
        NavItem("AI Coach", Icons.Default.AutoAwesome, 3),
        NavItem("Progress", Icons.Default.Insights, 4),
        NavItem("System", Icons.Default.AccountTree, 5)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = (selectedTab == item.index)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(item.index) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            selectedTextColor = IndigoPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.label.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Organization & Project Header
            NexoraBrandHeader(
                student = student,
                onProfileClick = { viewModel.setSelectedTab(6) } // Tab 6: Profile screen
            )

            // 2. Notification / Reschedule Toast Banner
            notification?.let { banner ->
                NotificationBanner(
                    message = banner.message,
                    isAlert = banner.isAlert,
                    onDismiss = { viewModel.clearNotification() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Screen content based on active tab
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> PlannerScreen(
                        student = student,
                        schedule = schedule,
                        onCompleteTask = { taskId -> viewModel.markTaskCompleted(taskId) },
                        onMarkMissed = { taskId -> viewModel.markTaskMissed(taskId) },
                        onFinishEarly = { taskId -> viewModel.finishEarly(taskId) },
                        onAdjustTime = { hours -> viewModel.updateAvailableStudyTime(hours) },
                        onAddNewTask = { showAddTaskDialog = true },
                        onExplainSchedule = { viewModel.setSelectedTab(3) },
                        isNotificationBarPinned = isNotificationBarEnabled,
                        onToggleNotificationBar = handleToggleNotificationBar
                    )

                    1 -> TasksScreen(
                        tasks = tasks,
                        onCompleteTask = { taskId -> viewModel.markTaskCompleted(taskId) },
                        onMarkMissed = { taskId -> viewModel.markTaskMissed(taskId) },
                        onDeleteTask = { taskId -> viewModel.deleteTask(taskId) },
                        onAddNewTask = { showAddTaskDialog = true }
                    )

                    2 -> RoadmapScreen(
                        student = student,
                        skills = skills,
                        onUpdateSkill = { skillId, status, progressVal ->
                            viewModel.updateSkillProgress(skillId, status, progressVal)
                        },
                        onAddSkillToPlanner = { skill ->
                            viewModel.addSkillAsStudyTask(skill)
                        }
                    )

                    3 -> AiCoachScreen(
                        aiGuidanceText = aiGuidanceText,
                        isLoading = isAiLoading,
                        onAskQuestion = { question -> viewModel.fetchAiGuidance(question) }
                    )

                    4 -> ProgressScreen(
                        tasks = tasks,
                        skills = skills,
                        sessions = sessions,
                        recentProgress = progress,
                        totalMinutesStudied = totalMinutes ?: 0
                    )

                    5 -> ArchitectureScreen()

                    6 -> ProfileScreen(
                        student = student,
                        onEditProfile = { showEditProfileDialog = true }
                    )
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { name, cat, desc, hrs, imp, dur, careerVal, canSplit, notes ->
                viewModel.addTask(
                    taskName = name,
                    category = cat,
                    deadlineDesc = desc,
                    deadlineHoursFromNow = hrs,
                    importance = imp,
                    durationMinutes = dur,
                    careerValue = careerVal,
                    canBeSplit = canSplit,
                    notes = notes
                )
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        student?.let { currentStudent ->
            EditProfileDialog(
                student = currentStudent,
                onDismiss = { showEditProfileDialog = false },
                onSave = { updated -> viewModel.updateStudentProfile(updated) }
            )
        }
    }
}

