package android.ai.jetpackdemonstration.ui.sideeffects.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RememberUpdatedStateDemo(modifier: Modifier = Modifier) {
    // Shared live event log
    val logMessages = remember { mutableStateListOf<String>() }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    fun addLog(msg: String) {
        logMessages.add(0, "[${timeFormatter.format(Date())}] $msg")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── SECTION 1: THE PROBLEM (STALE CAPTURE TRAP) ──
        SectionHeader(
            number = "1",
            title = "The Stale Lambda Trap (WITHOUT rememberUpdatedState)",
            subtitle = "LaunchedEffect(Unit) captures onTimeout at start. If onTimeout updates mid-flight, the effect invokes the OLD stale lambda!"
        )
        StaleCaptureDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 2: THE SOLUTION (rememberUpdatedState) ──
        SectionHeader(
            number = "2",
            title = "The Solution (WITH rememberUpdatedState)",
            subtitle = "rememberUpdatedState wraps onTimeout in a State. The effect never restarts, but reads the LATEST callback when done!"
        )
        UpdatedStateDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 3: WHY NOT JUST USE KEYED LAUNCHED EFFECT? ──
        SectionHeader(
            number = "3",
            title = "Why Not Just Key LaunchedEffect(onTimeout)?",
            subtitle = "If onTimeout is passed as a key, EVERY recomposition cancels and restarts the 5-second timer from 0, never finishing!"
        )
        KeyedRestartTrapDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── LIVE EVENT LOG ──
        LiveExecutionLogCard(
            logs = logMessages,
            onClear = { logMessages.clear() }
        )
    }
}

// ─── SECTION 1: STALE CAPTURE TRAP ───────────────────────────────────

@Composable
private fun StaleCaptureDemoCard(onLog: (String) -> Unit) {
    var selectedOption by remember { mutableStateOf("Red") }
    var timerRunning by remember { mutableStateOf(false) }

    DemoCard(label = "Stale Lambda Trap (Fails to capture mid-flight changes)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "1. Tap 'Start 4s Timer'\n2. Quickly change option to 'Blue' or 'Green' while countdown is active!",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Red", "Blue", "Green").forEach { option ->
                    FilterChip(
                        selected = selectedOption == option,
                        onClick = {
                            selectedOption = option
                            onLog("[Stale Demo] Option changed to: $option")
                        },
                        label = { Text(option) }
                    )
                }
            }

            if (timerRunning) {
                // Child component containing the buggy effect
                StaleTimerChild(
                    onTimeout = {
                        onLog("❌ [Stale Demo] Callback executed! Saw value: \"$selectedOption\"")
                        timerRunning = false
                    }
                )
            } else {
                Button(
                    onClick = {
                        onLog("[Stale Demo] Timer STARTED with initial option: \"$selectedOption\"")
                        timerRunning = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Start 4s Timer (Stale)")
                }
            }
        }
    }
}

/** ❌ BUGGY COMPONENT: Captures onTimeout at launch time and never updates its reference! */
@Composable
private fun StaleTimerChild(onTimeout: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val totalMs = 4000
        val steps = 20
        repeat(steps) {
            delay((totalMs / steps).toLong())
            progress += 1f / steps
        }
        // ❌ Invokes the stale lambda captured 4 seconds ago!
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Text("Timer Running (4s)... change option now!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    }
}

// ─── SECTION 2: THE SOLUTION (rememberUpdatedState) ──────────────────

@Composable
private fun UpdatedStateDemoCard(onLog: (String) -> Unit) {
    var selectedOption by remember { mutableStateOf("Red") }
    var timerRunning by remember { mutableStateOf(false) }

    DemoCard(label = "Correct Solution (rememberUpdatedState)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "1. Tap 'Start 4s Timer'\n2. Change option to 'Blue' or 'Green' while countdown is running.\n3. Notice it correctly invokes the LATEST option!",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Red", "Blue", "Green").forEach { option ->
                    FilterChip(
                        selected = selectedOption == option,
                        onClick = {
                            selectedOption = option
                            onLog("[UpdatedState Demo] Option changed to: $option")
                        },
                        label = { Text(option) }
                    )
                }
            }

            if (timerRunning) {
                // Child component using rememberUpdatedState
                CorrectTimerChild(
                    onTimeout = {
                        onLog("✅ [UpdatedState Demo] Callback executed! Successfully saw LATEST value: \"$selectedOption\"")
                        timerRunning = false
                    }
                )
            } else {
                Button(
                    onClick = {
                        onLog("[UpdatedState Demo] Timer STARTED with initial option: \"$selectedOption\"")
                        timerRunning = true
                    }
                ) {
                    Text("Start 4s Timer (UpdatedState)")
                }
            }
        }
    }
}

/** ✅ CORRECT COMPONENT: Uses rememberUpdatedState to capture fresh lambda reference without restarting! */
@Composable
private fun CorrectTimerChild(onTimeout: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    // ✅ rememberUpdatedState: wraps onTimeout in a State.
    // Every recomposition updates currentOnTimeout.value,
    // but the LaunchedEffect(Unit) below is NEVER cancelled or restarted!
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(Unit) {
        val totalMs = 4000
        val steps = 20
        repeat(steps) {
            delay((totalMs / steps).toLong())
            progress += 1f / steps
        }
        // ✅ Calls the LATEST lambda value stored in currentOnTimeout!
        currentOnTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Text("Timer Running (4s)... change option now!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    }
}

// ─── SECTION 3: KEYED RESTART TRAP ───────────────────────────────────

@Composable
private fun KeyedRestartTrapDemoCard(onLog: (String) -> Unit) {
    var clickCount by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }

    DemoCard(label = "Keyed Restart Trap (LaunchedEffect(onTimeout))") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "What if you passed onTimeout as a key? Every time parent recomposes, a new lambda is created. The timer cancels and restarts from 0 over and over!",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        clickCount++
                        onLog("[Restart Trap] Parent recomposed! clickCount = $clickCount")
                    }
                ) {
                    Text("Recompose Parent (Clicks: $clickCount)")
                }

                OutlinedButton(
                    onClick = { timerRunning = !timerRunning }
                ) {
                    Text(if (timerRunning) "Stop" else "Start Timer")
                }
            }

            if (timerRunning) {
                KeyedTimerChild(
                    onTimeout = {
                        onLog("🎉 [Restart Trap] Miracle! Timer actually finished!")
                        timerRunning = false
                    },
                    onRestart = { onLog("⚠️ [Restart Trap] Timer CANCELLED & RESTARTS from 0 because key changed!") }
                )
            }
        }
    }
}

/** ⚠️ FLAWED ATTEMPT: Keying on onTimeout causes constant cancellation on parent recompositions! */
@Composable
private fun KeyedTimerChild(
    onTimeout: () -> Unit,
    onRestart: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }

    // ⚠️ Flawed: onTimeout changes on EVERY parent recomposition (anonymous lambda)
    LaunchedEffect(onTimeout) {
        onRestart()
        progress = 0f
        val totalMs = 3000
        val steps = 15
        repeat(steps) {
            delay((totalMs / steps).toLong())
            progress += 1f / steps
        }
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text("Timer (3s). Tap 'Recompose Parent' while this is running!", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
    }
}

// ─── LIVE EXECUTION LOG CARD ─────────────────────────────────────────

@Composable
private fun LiveExecutionLogCard(
    logs: List<String>,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Execution Event Log",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Clear, contentDescription = "Clear logs")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Text(
                text = "Interact with the controls above to observe callback capture behavior...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                logs.forEach { log ->
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = when {
                            "❌" in log -> MaterialTheme.colorScheme.error
                            "✅" in log || "🎉" in log -> MaterialTheme.colorScheme.primary
                            "⚠️" in log -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

// ─── SHARED UI HELPERS ──────────────────────────────────────────────

@Composable
private fun SectionHeader(number: String, title: String, subtitle: String) {
    Column {
        Text(
            text = "Section $number — $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DemoCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}
