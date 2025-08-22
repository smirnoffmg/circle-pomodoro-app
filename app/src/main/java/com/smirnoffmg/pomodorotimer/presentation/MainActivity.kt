package com.smirnoffmg.pomodorotimer.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.smirnoffmg.pomodorotimer.analytics.AnalyticsHelper
import com.smirnoffmg.pomodorotimer.presentation.ui.screens.MainTimerScreen
import com.smirnoffmg.pomodorotimer.presentation.ui.theme.PomodoroTimerTheme
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.MainTimerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private val viewModel: MainTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PomodoroTimerTheme {
                MainTimerScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        analyticsHelper.logScreenView("MainTimerScreen")
        
        // Refresh daily statistics when app comes to foreground
        viewModel.loadDailyStatistics()
        
        // Force widget update to ensure correct layout
        com.smirnoffmg.pomodorotimer.widget.CircleTimerWidget.updateAllWidgets(this)
    }
}
