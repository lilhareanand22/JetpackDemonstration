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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── MOCK SYSTEM SERVICE (BROADCAST / SENSOR LISTENER) ────────────────

class MockSensorService {
    private val listeners = mutableListOf<(String) -> Unit>()
    var isListening: Boolean = false
        private set

    fun registerListener(listener: (String) -> Unit) {
        listeners.add(listener)
        isListening = true
    }

    fun unregisterListener(listener: (String) -> Unit) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            isListening = false
        }
    }

    fun emitEvent(event: String) {
        listeners.forEach { it(event) }
    }
}

// ─── MAIN DEMO SCREEN ────────────────────────────────────────────────

@Composable
fun DisposableEffectDemo(modifier: Modifier = Modifier) {
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
        // ── SECTION 1: LIFECYCLE EVENT OBSERVER DEMO ──
        SectionHeader(
            number = "1",
            title = "LifecycleEventObserver via DisposableEffect",
            subtitle = "Registers an observer on enter composition. onDispose guarantees unregistration when leaving composition!"
        )
        LifecycleObserverDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 2: SYSTEM SERVICE / LISTENER CLEANUP ──
        SectionHeader(
            number = "2",
            title = "Mock Sensor / Broadcast Listener Cleanup",
            subtitle = "Simulates registering a hardware sensor or broadcast listener and verifies zero memory leaks."
        )
        SensorListenerDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 3: KEYED DISPOSABLE EFFECT (Teardown & Re-setup) ──
        SectionHeader(
            number = "3",
            title = "Keyed DisposableEffect (Dispose old & Setup new)",
            subtitle = "When key changes, onDispose runs for the old key before the new effect block executes."
        )
        KeyedDisposableDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── LIVE EVENT LOG ──
        LiveExecutionLogCard(
            logs = logMessages,
            onClear = { logMessages.clear() }
        )
    }
}

// ─── SECTION 1: LIFECYCLE OBSERVER DEMO ──────────────────────────────

@Composable
private fun LifecycleObserverDemoCard(onLog: (String) -> Unit) {
    var isObserverActive by remember { mutableStateOf(false) }

    DemoCard(label = "Lifecycle Observer (LocalLifecycleOwner)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Toggle the observer below to see register on enter and onDispose on leave:",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isObserverActive) "Status: Observing Lifecycle" else "Status: Inactive")
                Button(
                    onClick = { isObserverActive = !isObserverActive }
                ) {
                    Text(if (isObserverActive) "Stop Observer" else "Start Observer")
                }
            }

            if (isObserverActive) {
                ObservedLifecycleChild(onLog = onLog)
            }
        }
    }
}

@Composable
private fun ObservedLifecycleChild(
    onLog: (String) -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var currentLifecycleEvent by remember { mutableStateOf("Observing...") }

    // ✅ DisposableEffect with lifecycleOwner as key
    DisposableEffect(lifecycleOwner) {
        onLog("DisposableEffect: Registering LifecycleEventObserver...")
        val observer = LifecycleEventObserver { _, event ->
            currentLifecycleEvent = event.name
            onLog("Lifecycle Event received: ${event.name}")
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // ✅ MANDATORY onDispose BLOCK:
        // Called when this composable leaves composition OR if lifecycleOwner key changes!
        onDispose {
            onLog("onDispose: Unregistering LifecycleEventObserver -> Cleaned up!")
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        Column {
            Text("Active Lifecycle Observer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Current Event: $currentLifecycleEvent", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─── SECTION 2: SENSOR LISTENER CLEANUP DEMO ─────────────────────────

@Composable
private fun SensorListenerDemoCard(onLog: (String) -> Unit) {
    val sensorService = remember { MockSensorService() }
    var isSubscribed by remember { mutableStateOf(false) }
    var latestSensorReading by remember { mutableStateOf("No data yet") }

    DemoCard(label = "Custom Hardware / Service Listener") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Simulates registering a sensor listener with a strict unregister onDispose:",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { isSubscribed = !isSubscribed }) {
                    Text(if (isSubscribed) "Unsubscribe Sensor" else "Subscribe Sensor")
                }

                OutlinedButton(
                    onClick = {
                        val fakeValue = "${(20..40).random()}°C / ${(40..80).random()}% humidity"
                        onLog("Sensor Hardware Emitted: $fakeValue")
                        sensorService.emitEvent(fakeValue)
                    },
                    enabled = isSubscribed
                ) {
                    Text("Trigger Sensor Pulse")
                }
            }

            if (isSubscribed) {
                // Subscribed Child using DisposableEffect
                SensorListenerChild(
                    service = sensorService,
                    onReading = { reading ->
                        latestSensorReading = reading
                        onLog("Composable UI received reading: $reading")
                    },
                    onLog = onLog
                )
            }

            Text(
                text = "Service status: ${if (sensorService.isListening) "🟢 Hardware Active (1 listener)" else "🔴 Idle (0 listeners)"}",
                style = MaterialTheme.typography.labelSmall,
                color = if (sensorService.isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = "Latest Reading: $latestSensorReading",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SensorListenerChild(
    service: MockSensorService,
    onReading: (String) -> Unit,
    onLog: (String) -> Unit
) {
    val currentOnReading by rememberUpdatedState(onReading)

    DisposableEffect(service) {
        val listener: (String) -> Unit = { data ->
            currentOnReading(data)
        }

        onLog("DisposableEffect: Registered listener with MockSensorService")
        service.registerListener(listener)

        onDispose {
            onLog("onDispose: Unregistered listener from MockSensorService -> No leak!")
            service.unregisterListener(listener)
        }
    }
}

// ─── SECTION 3: KEYED DISPOSABLE EFFECT ──────────────────────────────

@Composable
private fun KeyedDisposableDemoCard(onLog: (String) -> Unit) {
    var selectedChannel by remember { mutableStateOf("Tech") }

    DemoCard(label = "Keyed DisposableEffect (Channel Switcher)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "When channel key changes: onDispose runs for the old channel BEFORE setup runs for the new channel!",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Tech", "Finance", "Sports").forEach { channel ->
                    FilterChip(
                        selected = selectedChannel == channel,
                        onClick = { selectedChannel = channel },
                        label = { Text(channel) }
                    )
                }
            }

            // Keyed DisposableEffect running inside this scope
            KeyedChannelSubscription(channel = selectedChannel, onLog = onLog)
        }
    }
}

@Composable
private fun KeyedChannelSubscription(channel: String, onLog: (String) -> Unit) {
    // ✅ Keyed on channel: Whenever channel changes, old is disposed, new is initialized!
    DisposableEffect(channel) {
        onLog("DisposableEffect: Subscribing to channel: [$channel]")

        onDispose {
            onLog("onDispose: Cleaned up subscription for channel: [$channel]")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Text("Currently connected to socket stream: [$channel]", fontWeight = FontWeight.Bold)
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
                text = "Interact with the controls above to observe DisposableEffect and onDispose lifecycle...",
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
                            "onDispose" in log || "Unregister" in log -> MaterialTheme.colorScheme.error
                            "Register" in log || "Subscribing" in log -> MaterialTheme.colorScheme.primary
                            "Lifecycle Event" in log || "Emitted" in log -> MaterialTheme.colorScheme.tertiary
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
