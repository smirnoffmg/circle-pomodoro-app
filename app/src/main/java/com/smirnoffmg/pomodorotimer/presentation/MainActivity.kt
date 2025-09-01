package com.smirnoffmg.pomodorotimer.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smirnoffmg.pomodorotimer.analytics.AnalyticsHelper
import com.smirnoffmg.pomodorotimer.notification.NotificationPermissionManager
import com.smirnoffmg.pomodorotimer.presentation.ui.screens.mainTimerScreen
import com.smirnoffmg.pomodorotimer.presentation.ui.screens.timerSettingsScreen
import com.smirnoffmg.pomodorotimer.presentation.ui.theme.pomodoroTimerTheme
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.MainTimerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var notificationPermissionManager: NotificationPermissionManager

    private val viewModel: MainTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            pomodoroTimerTheme {
                pomodoroApp(viewModel = viewModel)
            }
        }

        // Request notification permissions if needed (API 33+) - do this after setContent
        checkNotificationPermissions()
    }

    private fun checkNotificationPermissions() {
        // Check if notifications need permissions and request them
        try {
            if (!notificationPermissionManager.isNotificationPermissionGranted()) {
                notificationPermissionManager.checkAndRequestPermissionIfNeeded(
                    activity = this,
                    onPermissionGranted = {
                        // Notifications are enabled, good to go
                    },
                    onPermissionDenied = {
                        // User denied notification permission
                        // App will still work but with limited notification features
                    },
                )
            }
        } catch (e: Exception) {
            // If there's an error with permission checking, just continue
            // The app should still work without notifications
        }
    }

    override fun onResume() {
        super.onResume()
        analyticsHelper.logScreenView("MainTimerScreen")

        // Fix: Automatically reconnect to running service when app is resumed
        viewModel.reconnectToServiceIfRunning()

        // Refresh daily statistics when app comes to foreground
        viewModel.loadDailyStatistics()

        // Reload timer settings to pick up any changes from settings screen
        viewModel.reloadTimerSettings()
    }
}

@Composable
fun pomodoroApp(viewModel: MainTimerViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "timer",
    ) {
        composable("timer") {
            mainTimerScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
            )
        }

        composable("settings") {
            timerSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
