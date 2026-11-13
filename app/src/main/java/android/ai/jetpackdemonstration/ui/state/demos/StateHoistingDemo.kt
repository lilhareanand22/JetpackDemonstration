package android.ai.jetpackdemonstration.ui.state.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─── MAIN DEMO SCREEN ────────────────────────────────────────────────

@Composable
fun StateHoistingDemo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── SECTION 1: Stateful Counter (BAD — internal state, not reusable) ──
        SectionHeader(
            number = "1",
            title = "Stateful Counter (NOT hoisted)",
            subtitle = "State lives INSIDE the composable — the parent cannot read or control it."
        )
        StatefulCounter()
        StatefulCounter() // Two independent instances — cannot share count

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 2: Stateless Counter (GOOD — state hoisted to parent) ──
        SectionHeader(
            number = "2",
            title = "Stateless Counter (State Hoisted)",
            subtitle = "State is lifted to the parent, passed DOWN as value, events flow UP via callbacks."
        )
        HoistedCounterSection()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 3: Hoisted TextField — shared input between siblings ──
        SectionHeader(
            number = "3",
            title = "Hoisted TextField — Two siblings sharing state",
            subtitle = "Parent holds the text, passes same value to two children. Both stay in sync."
        )
        HoistedTextFieldSection()
    }
}

// ─── SECTION 1: STATEFUL (ANTI-PATTERN FOR REUSE) ────────────────────

/**
 * ❌ STATEFUL composable — owns its own state.
 *
 * - Cannot be controlled by the parent.
 * - Cannot share state with siblings.
 * - Hard to test (need to click the button to change state).
 */
@Composable
private fun StatefulCounter() {
    // State is trapped inside this composable
    var count by remember { mutableIntStateOf(0) }

    DemoCard(label = "Stateful Counter") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Count: $count",
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { count-- }) { Text("-") }
                Button(onClick = { count++ }) { Text("+") }
            }
        }
    }
}

// ─── SECTION 2: STATELESS + PARENT OWNS STATE (STATE HOISTING) ───────

@Composable
private fun HoistedCounterSection() {
    // ✅ State is HOISTED to this parent composable
    var sharedCount by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Parent holds count = $sharedCount",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Both children display the SAME count — perfectly in sync
        StatelessCounter(
            label = "Child A",
            count = sharedCount,               // State flows DOWN
            onIncrement = { sharedCount++ },    // Event flows UP
            onDecrement = { sharedCount-- }     // Event flows UP
        )
        StatelessCounter(
            label = "Child B",
            count = sharedCount,               // Same state flows DOWN
            onIncrement = { sharedCount++ },    // Same event flows UP
            onDecrement = { sharedCount-- }     // Same event flows UP
        )

        OutlinedButton(
            onClick = { sharedCount = 0 },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Parent: Reset to 0")
        }
    }
}

/**
 * ✅ STATELESS composable — does NOT own any state.
 *
 * - [count] is the current value (State flows DOWN from parent).
 * - [onIncrement] / [onDecrement] are callbacks (Events flow UP to parent).
 * - Fully reusable, testable, and previewable.
 */
@Composable
private fun StatelessCounter(
    label: String,
    count: Int,                        // State flows DOWN
    onIncrement: () -> Unit,           // Event flows UP
    onDecrement: () -> Unit,           // Event flows UP
    modifier: Modifier = Modifier
) {
    DemoCard(label = label, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Count: $count",
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDecrement) { Text("-") }
                Button(onClick = onIncrement) { Text("+") }
            }
        }
    }
}

// ─── SECTION 3: HOISTED TEXTFIELD — SIBLING SYNC ────────────────────

@Composable
private fun HoistedTextFieldSection() {
    // ✅ State is HOISTED to the parent
    var name by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Parent state: \"$name\"",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Child A: Text field to WRITE the name
        NameInput(
            label = "Child A — TextField",
            value = name,                       // State flows DOWN
            onValueChange = { name = it }       // Event flows UP
        )

        // Child B: Greeting display that READS the same name
        NameGreeting(name = name)               // State flows DOWN

        Spacer(Modifier.height(4.dp))
        OutlinedButton(
            onClick = { name = "" },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Parent: Clear name")
        }
    }
}

/** Stateless TextField — receives value, emits changes up. */
@Composable
private fun NameInput(
    label: String,
    value: String,                              // State flows DOWN
    onValueChange: (String) -> Unit,            // Event flows UP
    modifier: Modifier = Modifier
) {
    DemoCard(label = label, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Enter your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Stateless display — only reads state, produces no events. */
@Composable
private fun NameGreeting(
    name: String,
    modifier: Modifier = Modifier
) {
    DemoCard(label = "Child B — Greeting", modifier = modifier) {
        Text(
            text = if (name.isBlank()) "Hello, World!" else "Hello, $name!",
            style = MaterialTheme.typography.headlineSmall
        )
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
