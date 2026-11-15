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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── MOCK EXTERNAL SYSTEM (ANALYTICS / NON-COMPOSE SDK) ───────────────

class MockAnalyticsTracker {
    var loggedScreenViews = mutableListOf<String>()
        private set
    var currentUserTier: String = "Free"
        private set

    fun setUserTierProperty(tier: String) {
        currentUserTier = tier
    }

    fun logEvent(event: String) {
        loggedScreenViews.add(event)
    }
}

// ─── MAIN DEMO SCREEN ────────────────────────────────────────────────

@Composable
fun SideEffectDemo(modifier: Modifier = Modifier) {
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
        // ── SECTION 1: PUBLISHING STATE TO NON-COMPOSE OBJECTS ──
        SectionHeader(
            number = "1",
            title = "Syncing State with Non-Compose Objects (Analytics SDK)",
            subtitle = "SideEffect runs on every SUCCESSFUL recomposition, keeping external objects in lockstep with UI state."
        )
        ExternalSyncDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 2: WHY NOT CALL CODE DIRECTLY IN COMPOSITION? ──
        SectionHeader(
            number = "2",
            title = "Direct Composable Body vs. SideEffect",
            subtitle = "Code in composable body can run during aborted compositions. SideEffect guarantees execution ONLY after commit!"
        )
        CommitGuaranteeDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── LIVE EVENT LOG ──
        LiveExecutionLogCard(
            logs = logMessages,
            onClear = { logMessages.clear() }
        )
    }
}

// ─── SECTION 1: EXTERNAL SYNC DEMO ───────────────────────────────────

@Composable
private fun ExternalSyncDemoCard(onLog: (String) -> Unit) {
    var userTier by remember { mutableStateOf("Free") }
    var pointsCount by remember { mutableIntStateOf(100) }
    val analyticsTracker = remember { MockAnalyticsTracker() }

    // ✅ SideEffect: Executes on EVERY successful recomposition.
    // Perfect for publishing updated Compose state to external non-Compose SDKs (Firebase, Crashlytics, etc.)
    SideEffect {
        analyticsTracker.setUserTierProperty(userTier)
        onLog("SideEffect executed -> Synced userTier [${analyticsTracker.currentUserTier}] with external MockAnalyticsTracker")
    }

    DemoCard(label = "Syncing External Analytics State") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Change user tier or points. SideEffect syncs external object on every successful recomposition:",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Free", "Pro", "Enterprise").forEach { tier ->
                    FilterChip(
                        selected = userTier == tier,
                        onClick = { userTier = tier },
                        label = { Text(tier) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Points: $pointsCount", fontWeight = FontWeight.Bold)
                Button(onClick = { pointsCount += 50 }) {
                    Text("+50 Points (Recomposes)")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "External SDK Tier Property: ${analyticsTracker.currentUserTier}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── SECTION 2: COMMIT GUARANTEE DEMO ────────────────────────────────

@Composable
private fun CommitGuaranteeDemoCard(onLog: (String) -> Unit) {
    var counter by remember { mutableIntStateOf(0) }
    var sideEffectRunCount by remember { mutableIntStateOf(0) }

    // ✅ SideEffect guarantees that composition completed successfully and was applied to the screen
    SideEffect {
        sideEffectRunCount++
        onLog("SideEffect: Composition successfully COMMITTED. Run count = $sideEffectRunCount")
    }

    DemoCard(label = "Composition Commit Guarantee") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Rule: Never mutate external state or trigger logs directly in the composable body. Always wrap in SideEffect!",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Counter State: $counter", style = MaterialTheme.typography.titleMedium)
                    Text("Committed passes: $sideEffectRunCount", style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = { counter++ }) {
                    Text("Increment Counter")
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
                text = "Interact with the controls above to observe SideEffect execution logs...",
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
                            "COMMITTED" in log -> MaterialTheme.colorScheme.tertiary
                            "Synced" in log -> MaterialTheme.colorScheme.primary
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
