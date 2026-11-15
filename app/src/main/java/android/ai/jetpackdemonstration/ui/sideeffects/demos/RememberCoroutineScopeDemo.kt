package android.ai.jetpackdemonstration.ui.sideeffects.demos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RememberCoroutineScopeDemo(modifier: Modifier = Modifier) {
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
        // ── SECTION 1: WHY WE NEED IT (onClick vs Composition) ──
        SectionHeader(
            number = "1",
            title = "Launching Coroutines from onClick Callbacks",
            subtitle = "LaunchedEffect CANNOT be called inside onClick. rememberCoroutineScope() provides a managed scope."
        )
        ManualLaunchDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 2: SUSPENDING ANIMATIONS (animateTo) ──
        SectionHeader(
            number = "2",
            title = "Manual Animation Trigger (Animatable.animateTo)",
            subtitle = "Triggering and managing smooth physics/tween animations on button clicks using coroutines."
        )
        AnimatedBoxDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 3: PROGRAMMATIC SCROLL & SNACKBAR ──
        SectionHeader(
            number = "3",
            title = "Programmatic Scroll & Snackbar Coordination",
            subtitle = "Call suspending animateScrollToItem() and snackbarHostState.showSnackbar() from user actions."
        )
        ScrollAndSnackbarDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── LIVE EVENT LOG ──
        LiveExecutionLogCard(
            logs = logMessages,
            onClear = { logMessages.clear() }
        )
    }
}

// ─── SECTION 1: MANUAL LAUNCH DEMO ───────────────────────────────────

@Composable
private fun ManualLaunchDemoCard(onLog: (String) -> Unit) {
    // ✅ rememberCoroutineScope: Returns a CoroutineScope bound to this point in composition
    val scope = rememberCoroutineScope()
    val activeJob = remember { mutableStateListOf<Job>() }

    DemoCard(label = "Basic Scope Usage in Callbacks") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Tap buttons to launch asynchronous background tasks directly from onClick:",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // Launching a managed coroutine from a regular Kotlin callback!
                        val job = scope.launch {
                            val id = (100..999).random()
                            onLog("Task #$id STARTED from onClick")
                            try {
                                delay(2000)
                                onLog("Task #$id COMPLETED successfully")
                            } catch (e: CancellationException) {
                                onLog("Task #$id CANCELLED")
                                throw e
                            }
                        }
                        activeJob.add(job)
                    }
                ) {
                    Text("Launch 2s Task")
                }

                OutlinedButton(
                    onClick = {
                        val cancelledCount = activeJob.count { it.isActive }
                        activeJob.forEach { it.cancel() }
                        activeJob.clear()
                        onLog("Manual cancel requested -> Cancelled $cancelledCount active tasks")
                    }
                ) {
                    Text("Cancel All")
                }
            }
        }
    }
}

// ─── SECTION 2: ANIMATION DEMO ───────────────────────────────────────

@Composable
private fun AnimatedBoxDemoCard(onLog: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    // Animatable holds an animatable float value
    val animOffset = remember { Animatable(0f) }

    DemoCard(label = "Suspending Animation Trigger (Animatable)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Animatable.animateTo() is a suspending function. Trigger it via scope.launch:",
                style = MaterialTheme.typography.bodySmall
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = animOffset.value.roundToInt(), y = 0) }
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            onLog("Starting animation to 220dp...")
                            animOffset.animateTo(
                                targetValue = 550f,
                                animationSpec = tween(durationMillis = 800)
                            )
                            onLog("Animation reached end position")
                        }
                    }
                ) {
                    Text("Animate Right")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            onLog("Resetting animation back to 0dp...")
                            animOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 500)
                            )
                            onLog("Animation returned to start")
                        }
                    }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

// ─── SECTION 3: SCROLL & SNACKBAR DEMO ────────────────────────────────

@Composable
private fun ScrollAndSnackbarDemoCard(onLog: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    DemoCard(label = "Programmatic Scroll & Snackbar Coordination") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Snackbar host embedded in card
            SnackbarHost(hostState = snackbarHostState)

            // Horizontal scrolling strip
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(25) { index ->
                    Card(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("#$index", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            onLog("Calling suspending animateScrollToItem(24)...")
                            listState.animateScrollToItem(24)
                            onLog("Scroll finished. Now showing Snackbar...")
                            val result = snackbarHostState.showSnackbar(
                                message = "Scrolled to end (#24)!",
                                actionLabel = "Scroll to Start",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onLog("User clicked 'Scroll to Start' in Snackbar!")
                                listState.animateScrollToItem(0)
                            }
                        }
                    }
                ) {
                    Text("Scroll End (#24)")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            onLog("Calling animateScrollToItem(0)...")
                            listState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Text("Scroll Start (#0)")
                }
            }
        }
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
                text = "Tap any action above to observe coroutine execution logs...",
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
                            "CANCELLED" in log -> MaterialTheme.colorScheme.error
                            "STARTED" in log -> MaterialTheme.colorScheme.primary
                            "COMPLETED" in log || "reached" in log -> MaterialTheme.colorScheme.tertiary
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
