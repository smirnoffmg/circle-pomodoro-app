package com.smirnoffmg.pomodorotimer.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession

@Composable
fun TimerScreen(viewModel: TimerViewModel = hiltViewModel()) {
    val pomodoroSessions by viewModel.pomodoroSessions.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { viewModel.addPomodoroSession(25 * 60 * 1000) }) {
            Text("Add 25-minute session")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(pomodoroSessions) { session ->
                PomodoroSessionItem(session)
            }
        }
    }
}

@Composable
fun PomodoroSessionItem(session: PomodoroSession) {
    Text("Session: ${session.id}, Type: ${session.type}, Duration: ${session.duration / 1000}s, Completed: ${session.isCompleted}")
}
