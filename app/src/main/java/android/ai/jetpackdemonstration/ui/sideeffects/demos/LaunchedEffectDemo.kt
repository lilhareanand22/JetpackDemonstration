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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LaunchedEffectDemo(modifier: Modifier = Modifier) {
    // Shared live event log so you can observe the exact execution and cancellation order
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
        // ── SECTION 1: ONE-SHOT EFFECT (key = Unit) ──
        SectionHeader(
            number = "1",
            title = "One-Shot LaunchedEffect (key = Unit)",
            subtitle = "Runs once when entering composition. Won't restart on recompositions. Cancels on leave."
        )
        OneShotDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 2: KEYED EFFECT (Cancelling & Restarting) ──
        SectionHeader(
            number = "2",
            title = "Keyed LaunchedEffect (Auto-Restart on Key Change)",
            subtitle = "When the key changes, Compose cancels the running coroutine and starts a new one."
        )
        KeyedRestartDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 3: REAL-WORLD USE CASE (Debounced Search) ──
        SectionHeader(
            number = "3",
            title = "Real-World Pattern: Debounced Query Fetch",
            subtitle = "Rapid typing cancels previous queries and only queries the network after user stops typing for 500ms."
        )
        DebouncedSearchDemoCard(onLog = ::addLog)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── LIVE EVENT LOG ──
        LiveExecutionLogCard(
            logs = logMessages,
            onClear = { logMessages.clear() }
        )
    }
}

// ─── SECTION 1: ONE-SHOT DEMO ────────────────────────────────────────

@Composable
private fun OneShotDemoCard(onLog: (String) -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    DemoCard(label = "One-Shot Lifecycle Demo") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Toggle Composable presence in tree:")
                Button(onClick = { isVisible = !isVisible }) {
                    Text(if (isVisible) "Remove Child" else "Add Child")
                }
            }

            if (isVisible) {
                ChildWithOneShotEffect(onLog = onLog)
            } else {
                Text(
                    text = "Child is currently NOT in composition.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChildWithOneShotEffect(onLog: (String) -> Unit) {
    var timerTicks by remember { mutableIntStateOf(0) }

    // ✅ LaunchedEffect(Unit): Starts when entering composition, cancels when leaving
    LaunchedEffect(Unit) {
        onLog("Child entered composition -> LaunchedEffect(Unit) STARTED")
        try {
            while (true) {
                delay(1000)
                timerTicks++
                onLog("Timer tick: $timerTicks sec")
            }
        } catch (e: CancellationException) {
            onLog("Child left composition -> LaunchedEffect(Unit) CANCELLED cleanly")
            throw e // Always rethrow CancellationException in Kotlin coroutines!
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        Text("Child Composable Active", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Running coroutine timer: $timerTicks seconds")
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

// ─── SECTION 2: KEYED RESTART DEMO ───────────────────────────────────

@Composable
private fun KeyedRestartDemoCard(onLog: (String) -> Unit) {
    var selectedCategoryId by remember { mutableIntStateOf(1) }
    var fetchedData by remember { mutableStateOf("Ready") }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ LaunchedEffect(selectedCategoryId):
    // Whenever selectedCategoryId changes, the ongoing coroutine is cancelled and a new one launches!
    LaunchedEffect(selectedCategoryId) {
        isLoading = true
        onLog("Category changed to #$selectedCategoryId -> Previous fetch cancelled, NEW fetch started")
        try {
            fetchedData = "Fetching data for category #$selectedCategoryId..."
            delay(1500) // Simulate network delay
            fetchedData = "SUCCESS: Loaded 15 items for Category #$selectedCategoryId"
            isLoading = false
            onLog("Fetch completed successfully for Category #$selectedCategoryId")
        } catch (e: CancellationException) {
            onLog("Fetch CANCELLED mid-flight for Category #$selectedCategoryId!")
            throw e
        }
    }

    DemoCard(label = "Keyed LaunchedEffect (Category Switcher)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Switch categories rapidly to see mid-flight cancellations:")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEach { id ->
                    FilterChip(
                        selected = selectedCategoryId == id,
                        onClick = { selectedCategoryId = id },
                        label = { Text("Category $id") }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(8.dp))
                        Text(fetchedData, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text(fetchedData, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── SECTION 3: DEBOUNCED SEARCH DEMO ────────────────────────────────

@Composable
private fun DebouncedSearchDemoCard(onLog: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf("Type above to search...") }
    var isSearching by remember { mutableStateOf(false) }

    // ✅ Keyed on searchQuery:
    // Every keystroke cancels the 500ms delay countdown and restarts it!
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResult = "Type above to search..."
            isSearching = false
            return@LaunchedEffect
        }

        isSearching = true
        onLog("Query changed: \"$searchQuery\" -> Waiting 500ms debounce...")
        try {
            // Debounce delay: if another keystroke arrives during these 500ms,
            // this coroutine is cancelled and the network call never fires!
            delay(500)
            onLog("Debounce passed! Executing network query for: \"$searchQuery\"")
            delay(800) // Simulated network call
            searchResult = "Found ${searchQuery.length} results for \"$searchQuery\""
            isSearching = false
            onLog("Query successful for \"$searchQuery\"")
        } catch (e: CancellationException) {
            onLog("Debounce cancelled because user continued typing!")
            throw e
        }
    }

    DemoCard(label = "Debounced Search Pattern") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search products (500ms debounce)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    text = searchResult,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
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
                text = "Interact with the controls above to observe effect lifecycles...",
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
                            "SUCCESS" in log -> MaterialTheme.colorScheme.tertiary
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
