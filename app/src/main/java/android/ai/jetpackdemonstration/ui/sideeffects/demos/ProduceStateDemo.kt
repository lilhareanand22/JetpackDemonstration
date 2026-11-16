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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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

// ─── STATE MODEL FOR NETWORK REPOSITORY ───────────────────────────────

sealed interface RepositoryResult {
    data object Loading : RepositoryResult
    data class Success(val userDetails: String) : RepositoryResult
    data class Failure(val error: String) : RepositoryResult
}

// ─── MOCK BACKEND DATA SOURCE ─────────────────────────────────────────

object FakeUserRepository {
    suspend fun fetchUserDetails(userId: Int): String {
        delay(1200) // Simulate network delay
        if (userId == 999) {
            throw RuntimeException("Network Error: User ID 999 not found!")
        }
        return "User Profile #$userId (Senior Android Architect, Tier: Premium)"
    }
}

// ─── MAIN DEMO SCREEN ────────────────────────────────────────────────

@Composable
fun ProduceStateDemo(modifier: Modifier = Modifier) {
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
        // ── SECTION 1: ASYNC REPOSITORY FETCH (SEALED RESULT) ──
        SectionHeader(
            number = "1",
            title = "Converting Coroutine Fetch to State<T>",
            subtitle = "produceState bridges async calls into immutable State. Key changes cancel and re-run automatically!"
        )
        AsyncRepositoryCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 2: STREAMING LIVE COUNTER (INTERVAL EMISSIONS) ──
        SectionHeader(
            number = "2",
            title = "Streaming State Producer (Continuous Ticks)",
            subtitle = "The producer coroutine can emit values over time by assigning 'value = ...' inside an active loop."
        )
        StreamingTickerCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── LIVE EVENT LOG ──
        LiveExecutionLogCard(
            logs = logMessages,
            onClear = { logMessages.clear() }
        )
    }
}

// ─── SECTION 1: ASYNC REPOSITORY FETCH ────────────────────────────────

@Composable
private fun AsyncRepositoryCard(onLog: (String) -> Unit) {
    var selectedUserId by remember { mutableIntStateOf(101) }

    // ✅ produceState: Launches a coroutine that sets `value = ...`
    // Automatically cancels and restarts if `selectedUserId` key changes!
    val userResult: State<RepositoryResult> = produceState<RepositoryResult>(
        initialValue = RepositoryResult.Loading,
        key1 = selectedUserId
    ) {
        // We are inside ProducerScope!
        value = RepositoryResult.Loading
        onLog("produceState: Started fetching details for user #$selectedUserId...")
        try {
            val details = FakeUserRepository.fetchUserDetails(selectedUserId)
            value = RepositoryResult.Success(details)
            onLog("produceState: Success for user #$selectedUserId -> $details")
        } catch (e: Exception) {
            value = RepositoryResult.Failure(e.message ?: "Unknown error")
            onLog("produceState: Failure for user #$selectedUserId -> ${e.message}")
        }
    }

    DemoCard(label = "produceState with Keyed Network Call") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Select a User ID to observe produceState cancel previous calls and emit Loading -> Success/Error:",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(101, 102, 999).forEach { id ->
                    FilterChip(
                        selected = selectedUserId == id,
                        onClick = { selectedUserId = id },
                        label = { Text(if (id == 999) "User 999 (Error)" else "User $id") }
                    )
                }
            }

            // Render current produceState value
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                when (val result = userResult.value) {
                    is RepositoryResult.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(8.dp))
                            Text("Loading user #$selectedUserId details from network...", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    is RepositoryResult.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text(result.userDetails, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    is RepositoryResult.Failure -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text(result.error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// ─── SECTION 2: STREAMING TICKER DEMO ─────────────────────────────────

@Composable
private fun StreamingTickerCard(onLog: (String) -> Unit) {
    var isTickerRunning by remember { mutableStateOf(false) }

    DemoCard(label = "Streaming State Producer (Continuous Emissions)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isTickerRunning) "Ticker Status: Active" else "Ticker Status: Paused")
                Button(onClick = { isTickerRunning = !isTickerRunning }) {
                    Text(if (isTickerRunning) "Pause Ticker" else "Start Ticker")
                }
            }

            if (isTickerRunning) {
                StreamingTickerChild(onLog = onLog)
            }
        }
    }
}

@Composable
private fun StreamingTickerChild(onLog: (String) -> Unit) {
    // ✅ produceState continuously emitting values on an interval
    val currentSecond: State<Int> = produceState(initialValue = 0) {
        onLog("Streaming produceState: Ticker loop started")
        while (true) {
            delay(1000)
            value += 1
            onLog("Streaming produceState emitted value = $value")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Text(
            text = "Live Ticker: ${currentSecond.value}s running",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
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
                text = "Select user IDs or toggle the ticker above to observe produceState execution...",
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
                            "Failure" in log -> MaterialTheme.colorScheme.error
                            "Success" in log -> MaterialTheme.colorScheme.primary
                            "Streaming" in log -> MaterialTheme.colorScheme.tertiary
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
