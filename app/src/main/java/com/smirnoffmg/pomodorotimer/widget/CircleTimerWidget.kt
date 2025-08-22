package com.smirnoffmg.pomodorotimer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.smirnoffmg.pomodorotimer.R
import com.smirnoffmg.pomodorotimer.service.TimerForegroundService

/**
 * Circle Timer Widget - Minimal and clean
 * 
 * Simple timer counter and play/pause button.
 * Follows Circle concept: minimal, reliable, transparent.
 */
class CircleTimerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_circle_timer)

        // Play/Pause button
        val playPauseIntent =
            Intent(context, TimerForegroundService::class.java).apply {
                action = TimerForegroundService.ACTION_START_TIMER
            }
        val playPausePendingIntent =
            PendingIntent.getService(
                context,
                0,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        views.setOnClickPendingIntent(R.id.widget_play_pause_button, playPausePendingIntent)

        // Update timer counter (placeholder - will be updated by service)
        views.setTextViewText(R.id.widget_timer_counter, "25:00")

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        super.onReceive(context, intent)
        
        // Update all widgets when timer state changes
        if (intent.action == ACTION_TIMER_STATE_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds =
                appWidgetManager.getAppWidgetIds(
                    ComponentName(context, CircleTimerWidget::class.java)
                )
            
            // Update all widgets
            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_TIMER_STATE_CHANGED = "com.smirnoffmg.pomodorotimer.TIMER_STATE_CHANGED"

        fun updateAllWidgets(context: Context) {
            val intent =
                Intent(context, CircleTimerWidget::class.java).apply {
                    action = ACTION_TIMER_STATE_CHANGED
                }
            context.sendBroadcast(intent)
        }

        fun updateTimerDisplay(
            context: Context,
            timeInSeconds: Long,
            isRunning: Boolean
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds =
                appWidgetManager.getAppWidgetIds(
                    ComponentName(context, CircleTimerWidget::class.java)
                )
            
            appWidgetIds.forEach { appWidgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_circle_timer)
                
                // Update timer counter
                val timeText = formatTime(timeInSeconds)
                views.setTextViewText(R.id.widget_timer_counter, timeText)
                
                // Update play/pause button
                val buttonText = if (isRunning) "⏸" else "▶"
                views.setTextViewText(R.id.widget_play_pause_button, buttonText)
                
                // Update button action
                val action =
                    if (isRunning) {
                        TimerForegroundService.ACTION_PAUSE_TIMER
                    } else {
                        TimerForegroundService.ACTION_START_TIMER
                    }
                
                val playPauseIntent =
                    Intent(context, TimerForegroundService::class.java).apply {
                        this.action = action
                    }
                val playPausePendingIntent =
                    PendingIntent.getService(
                        context,
                        0,
                        playPauseIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                views.setOnClickPendingIntent(R.id.widget_play_pause_button, playPausePendingIntent)
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun formatTime(timeInSeconds: Long): String {
            val minutes = timeInSeconds / 60
            val seconds = timeInSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}
