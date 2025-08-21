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
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord

@Composable
fun TimerScreen(viewModel: TimerViewModel = hiltViewModel()) {
    val timerRecords by viewModel.timerRecords.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { viewModel.addTimerRecord(25 * 60 * 1000) }) {
            Text("Add 25-minute record")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(timerRecords) { record ->
                TimerRecordItem(record)
            }
        }
    }
}

@Composable
fun TimerRecordItem(record: TimerRecord) {
    Text("Record: ${record.id}, Duration: ${record.durationSeconds}s, Timestamp: ${record.startTimestamp}")
}
