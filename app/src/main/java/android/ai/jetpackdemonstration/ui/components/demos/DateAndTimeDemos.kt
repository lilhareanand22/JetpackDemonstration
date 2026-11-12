package android.ai.jetpackdemonstration.ui.components.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDemoScreen(modifier: Modifier = Modifier) {
    var showDateDialog by remember { mutableStateOf(false) }
    var showRangeDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val dateRangePickerState = rememberDateRangePickerState()

    val formatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val selectedDateText = datePickerState.selectedDateMillis?.let {
        formatter.format(Date(it))
    } ?: "None"

    val startText = dateRangePickerState.selectedStartDateMillis?.let { formatter.format(Date(it)) }
    val endText = dateRangePickerState.selectedEndDateMillis?.let { formatter.format(Date(it)) }
    val selectedRangeText = if (startText != null && endText != null) "$startText – $endText" else "None"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selected Date: $selectedDateText", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { showDateDialog = true }) {
            Text("Open DatePickerDialog")
        }

        Text("Selected Range: $selectedRangeText", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { showRangeDialog = true }) {
            Text("Open DateRangePickerDialog")
        }
    }

    if (showDateDialog) {
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showRangeDialog) {
        DatePickerDialog(
            onDismissRequest = { showRangeDialog = false },
            confirmButton = {
                TextButton(onClick = { showRangeDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRangeDialog = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDemoScreen(modifier: Modifier = Modifier) {
    var useInput by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = 10,
        initialMinute = 30,
        is24Hour = false
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            FilterChip(
                selected = !useInput,
                onClick = { useInput = false },
                label = { Text("TimePicker") }
            )
            FilterChip(
                selected = useInput,
                onClick = { useInput = true },
                label = { Text("TimeInput") }
            )
        }

        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute),
            style = MaterialTheme.typography.headlineMedium
        )

        if (useInput) {
            TimeInput(state = timePickerState)
        } else {
            TimePicker(state = timePickerState)
        }
    }
}
