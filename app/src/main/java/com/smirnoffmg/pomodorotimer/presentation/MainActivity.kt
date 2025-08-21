package com.smirnoffmg.pomodorotimer.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smirnoffmg.pomodorotimer.analytics.AnalyticsHelper
import com.smirnoffmg.pomodorotimer.presentation.ui.theme.PomodoroTimerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PomodoroTimerTheme {
                TimerScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        analyticsHelper.logScreenView("TimerScreen")
    }
}
