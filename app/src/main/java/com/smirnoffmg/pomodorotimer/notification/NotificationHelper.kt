package com.smirnoffmg.pomodorotimer.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.smirnoffmg.pomodorotimer.R
import com.smirnoffmg.pomodorotimer.presentation.MainActivity
import com.smirnoffmg.pomodorotimer.service.TimerForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val channelManager: NotificationChannelManager,
        private val notificationSettings: NotificationSettings
    ) {
        companion object {
            const val TIMER_NOTIFICATION_ID = 1001
            const val BREAK_START_NOTIFICATION_ID = 1002
            const val BREAK_END_NOTIFICATION_ID = 1003
            const val SESSION_COMPLETE_NOTIFICATION_ID = 1004
            const val MILESTONE_NOTIFICATION_ID = 1005
        
            private const val PAUSE_ACTION_CODE = 100
            private const val RESUME_ACTION_CODE = 101
            private const val STOP_ACTION_CODE = 102
            private const val SKIP_BREAK_ACTION_CODE = 103
        }

        private val notificationManager: NotificationManager by lazy {
            context.getSystemService() ?: throw IllegalStateException("NotificationManager not available")
        }

        fun createTimerNotification(
            remainingTime: String,
            cycleType: String,
            isRunning: Boolean
        ): android.app.Notification? {
            if (!notificationSettings.isPersistentTimerNotificationEnabled) {
                return null
            }

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        
            val pendingIntent =
                PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val stateText = if (isRunning) cycleType else "Paused"
        
            val builder =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.TIMER_CHANNEL_ID)
                    .setContentTitle("Pomodoro Timer")
                    .setContentText("$remainingTime - $stateText")
                    .setSmallIcon(getTimerIcon(isRunning))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setSilent(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
        
            if (isRunning) {
                builder.addAction(createPauseAction())
                builder.addAction(createStopAction())
            } else {
                builder.addAction(createResumeAction())
                builder.addAction(createStopAction())
            }
        
            return builder.build()
        }

        fun createBreakNotification(
            remainingTime: String,
            breakType: String,
            isRunning: Boolean
        ): android.app.Notification? {
            if (!notificationSettings.isPersistentTimerNotificationEnabled) {
                return null
            }

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        
            val pendingIntent =
                PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val stateText = if (isRunning) breakType else "Paused"
        
            val builder =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.TIMER_CHANNEL_ID)
                    .setContentTitle("Break Time")
                    .setContentText("$remainingTime - $stateText")
                    .setSmallIcon(getBreakIcon())
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setSilent(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
        
            if (isRunning) {
                builder.addAction(createSkipBreakAction())
                builder.addAction(createStopAction())
            } else {
                builder.addAction(createResumeAction())
                builder.addAction(createStopAction())
            }
        
            return builder.build()
        }

        fun showBreakStartNotification(breakType: String) {
            if (!notificationSettings.isBreakNotificationsEnabled) {
                return
            }

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        
            val pendingIntent =
                PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.BREAKS_CHANNEL_ID)
                    .setContentTitle("Time for a $breakType!")
                    .setContentText("Take a moment to rest and recharge.")
                    .setSmallIcon(getBreakIcon())
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat
                            .BigTextStyle()
                            .bigText("Great work! Take a moment to rest and recharge. Your break has started.")
                    ).apply {
                        if (notificationSettings.isNotificationSoundEnabled) {
                            setSound(getBreakSound())
                        }
                        if (notificationSettings.isNotificationVibrationEnabled) {
                            setVibrate(longArrayOf(0, 300, 200, 300))
                        }
                    }.build()
        
            notificationManager.notify(BREAK_START_NOTIFICATION_ID, notification)
        }

        fun showBreakEndNotification() {
            if (!notificationSettings.isBreakNotificationsEnabled) {
                return
            }

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        
            val pendingIntent =
                PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.BREAKS_CHANNEL_ID)
                    .setContentTitle("Break Complete!")
                    .setContentText("Ready to get back to work?")
                    .setSmallIcon(getBreakIcon())
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat
                            .BigTextStyle()
                            .bigText("Your break is over. Ready to focus and be productive again?")
                    ).apply {
                        if (notificationSettings.isNotificationSoundEnabled) {
                            setSound(getBreakSound())
                        }
                        if (notificationSettings.isNotificationVibrationEnabled) {
                            setVibrate(longArrayOf(0, 300, 200, 300))
                        }
                    }.build()
        
            notificationManager.notify(BREAK_END_NOTIFICATION_ID, notification)
        }

        fun showSessionCompleteNotification(sessionCount: Int) {
            if (!notificationSettings.isSessionCompleteNotificationsEnabled) {
                return
            }

            if (sessionCount <= 0 || sessionCount > 1000) return

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        
            val pendingIntent =
                PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val title = "Session Complete! 🍅"
            val text =
                when (sessionCount) {
                    1 -> "Great start! You've completed your first session."
                    else -> "Well done! You've completed $sessionCount sessions."
                }
        
            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.PROGRESS_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(getSessionCompleteIcon())
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .apply {
                        if (notificationSettings.isNotificationSoundEnabled) {
                            setSound(getSessionCompleteSound())
                        }
                        if (notificationSettings.isNotificationVibrationEnabled) {
                            setVibrate(longArrayOf(0, 100, 100, 100))
                        }
                    }.build()
        
            notificationManager.notify(SESSION_COMPLETE_NOTIFICATION_ID, notification)
        }

        fun showMilestoneNotification(milestone: Int) {
            if (!notificationSettings.isMilestoneNotificationsEnabled) {
                return
            }

            if (milestone <= 0 || milestone > 10000) return

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        
            val pendingIntent =
                PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val title = "Milestone Reached! 🎉"
            val text = "Congratulations! You've completed $milestone productive sessions!"
        
            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.PROGRESS_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(getMilestoneIcon())
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .apply {
                        if (notificationSettings.isNotificationSoundEnabled) {
                            setSound(getMilestoneSound())
                        }
                        if (notificationSettings.isNotificationVibrationEnabled) {
                            setVibrate(longArrayOf(0, 200, 100, 200, 100, 200))
                        }
                    }.build()
        
            notificationManager.notify(MILESTONE_NOTIFICATION_ID, notification)
        }

        fun cancelNotification(notificationId: Int) {
            notificationManager.cancel(notificationId)
        }

        fun cancelAllNotifications() {
            notificationManager.cancelAll()
        }

        private fun createPauseAction(): NotificationCompat.Action {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_PAUSE_TIMER
                }
            val pendingIntent =
                PendingIntent.getService(
                    context, PAUSE_ACTION_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            return NotificationCompat.Action
                .Builder(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    pendingIntent
                ).build()
        }

        private fun createResumeAction(): NotificationCompat.Action {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_START_TIMER
                }
            val pendingIntent =
                PendingIntent.getService(
                    context, RESUME_ACTION_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            return NotificationCompat.Action
                .Builder(
                    android.R.drawable.ic_media_play,
                    "Resume",
                    pendingIntent
                ).build()
        }

        private fun createStopAction(): NotificationCompat.Action {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_STOP_TIMER
                }
            val pendingIntent =
                PendingIntent.getService(
                    context, STOP_ACTION_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            return NotificationCompat.Action
                .Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    pendingIntent
                ).build()
        }

        private fun createSkipBreakAction(): NotificationCompat.Action {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_SKIP_BREAK
                }
            val pendingIntent =
                PendingIntent.getService(
                    context, SKIP_BREAK_ACTION_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            return NotificationCompat.Action
                .Builder(
                    android.R.drawable.ic_media_next,
                    "Skip",
                    pendingIntent
                ).build()
        }

        private fun getTimerIcon(isRunning: Boolean): Int =
            if (isRunning) {
                R.drawable.ic_timer_running
            } else {
                R.drawable.ic_timer_paused
            }

        private fun getBreakIcon(): Int = R.drawable.ic_break

        private fun getSessionCompleteIcon(): Int = R.drawable.ic_session_complete

        private fun getMilestoneIcon(): Int = R.drawable.ic_milestone

        private fun getBreakSound(): android.net.Uri? = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI

        private fun getSessionCompleteSound(): android.net.Uri? = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI

        private fun getMilestoneSound(): android.net.Uri? = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
    }
