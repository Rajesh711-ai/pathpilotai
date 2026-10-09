package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ScheduleSlot
import com.example.data.model.StudentProfile

object PathPilotNotificationManager {

    const val CHANNEL_ID = "pathpilot_focus_channel"
    const val CHANNEL_NAME = "PathPilot Focus & Priority"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows your current focus block and explains what to do right now on the notification bar"
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays the current active task and the explanation of "Why this?" directly in the Android notification bar.
     */
    fun showPriorityNotification(
        context: Context,
        slot: ScheduleSlot?,
        student: StudentProfile? = null
    ) {
        createNotificationChannel(context)

        // Intent to launch the app when tapping the notification body
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_focus)
            .setColor(0xFF4F46E5.toInt()) // Indigo theme accent
            .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOngoing(true) // Pinned to status & notification bar

        if (slot != null) {
            val title = "🎯 Now: ${slot.taskName}"
            val subtitle = "${slot.startTime} – ${slot.endTime} • ${slot.category}"
            val bigText = buildString {
                append("▶ ").append(slot.taskName).append("\n")
                append("⏰ Scheduled: ").append(slot.startTime).append(" – ").append(slot.endTime)
                append(" (").append(slot.durationMinutes).append(" mins)\n")
                append("💡 Why this? ").append(
                    slot.explanationReason.ifBlank { "Top priority task calculated by Adaptive AI Decision Engine." }
                )
                if (student != null) {
                    append("\n🎯 Career Goal: ").append(student.careerGoal)
                }
            }

            builder.setContentTitle(title)
                .setContentText(subtitle)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle("PathPilot AI: What To Do Right Now")
                        .setSummaryText(slot.category)
                        .bigText(bigText)
                )

            // Quick Action: Mark Done directly from notification bar
            val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_DONE
                putExtra(NotificationActionReceiver.EXTRA_TASK_ID, slot.taskId)
            }
            val donePendingIntent = PendingIntent.getBroadcast(
                context,
                slot.taskId.toInt(),
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                android.R.drawable.checkbox_on_background,
                "Mark Done",
                donePendingIntent
            )

            // Action: Open Planner
            builder.addAction(
                android.R.drawable.ic_menu_today,
                "Open App",
                openAppPendingIntent
            )
        } else {
            builder.setContentTitle("PathPilot AI — All Tasks Cleared! 🎉")
                .setContentText("No urgent assignments right now. Tap to review career milestones.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("You have completed all scheduled tasks! Excellent focus. Next recommendation: Practice 30–45 mins on ${student?.careerGoal ?: "career milestones"}.")
                )
                .setOngoing(false)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not granted
        }
    }

    fun dismissNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
