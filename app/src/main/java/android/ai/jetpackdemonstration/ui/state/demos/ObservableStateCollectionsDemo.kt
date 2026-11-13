package android.ai.jetpackdemonstration.ui.state.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─── DATA MODEL ───────────────────────────────────────────────────────

data class TaskItem(
    val id: Int,
    val title: String,
    val isCompleted: Boolean = false
)

// ─── MAIN DEMO SCREEN ────────────────────────────────────────────────

@Composable
fun ObservableStateCollectionsDemo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── SECTION 1: ArrayList (Broken) vs mutableStateListOf (Reactive) ──
        SectionHeader(
            number = "1",
            title = "mutableListOf (BROKEN) vs. mutableStateListOf (REACTIVE)",
            subtitle = "Tap '+ Item' on both. Regular Kotlin mutableListOf mutates in-place without triggering recomposition!"
        )
        ListComparisonCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 2: IN-PLACE ELEMENT MUTATION GOTCHA ──
        SectionHeader(
            number = "2",
            title = "Item Mutation Gotcha (Why copy() is Required)",
            subtitle = "Mutating an object's internal property won't notify Snapshot State. Replacing it with .copy() triggers recomposition."
        )
        ItemMutationGotchaCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 3: mutableStateMapOf (Key-Value State) ──
        SectionHeader(
            number = "3",
            title = "mutableStateMapOf (Reactive Key-Value Map)",
            subtitle = "Tracks insertions, removals, and key-level updates with granular recomposition."
        )
        StateMapDemoCard()
    }
}

// ─── SECTION 1: LIST COMPARISON ──────────────────────────────────────

@Composable
private fun ListComparisonCard() {
    // ❌ WRONG: Standard Kotlin mutableList does NOT hook into Snapshot state!
    val regularList = remember { mutableListOf("Alpha", "Beta") }
    // A dummy state to force recomposition so you can verify regularList DID change in memory
    var forceRecomposeTrigger by remember { mutableIntStateOf(0) }

    // ✅ CORRECT: SnapshotStateList tracks additions and deletions automatically!
    val observableList = remember { mutableStateListOf("Alpha", "Beta") }

    DemoCard(label = "Comparison: Regular List vs. mutableStateListOf") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // --- Part A: Broken regular list ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "❌ mutableListOf (Size: ${regularList.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Items: ${regularList.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            // Mutating underlying list: NO RECOMPOSITION OCCURS!
                            regularList.add("Item ${regularList.size + 1}")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("+ Item (No UI update)")
                    }
                    OutlinedButton(
                        onClick = { forceRecomposeTrigger++ }
                    ) {
                        Text("Force Refresh UI")
                    }
                }
            }

            // --- Part B: Reactive SnapshotStateList ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "✅ mutableStateListOf (Size: ${observableList.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Items: ${observableList.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            observableList.add("Item ${observableList.size + 1}")
                        }
                    ) {
                        Text("+ Item (Instant UI update)")
                    }
                    if (observableList.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { observableList.removeAt(observableList.lastIndex) }
                        ) {
                            Text("- Last")
                        }
                    }
                }
            }
        }
    }
}

// ─── SECTION 2: ITEM MUTATION GOTCHA ─────────────────────────────────

@Composable
private fun ItemMutationGotchaCard() {
    val tasks = remember {
        mutableStateListOf(
            TaskItem(id = 1, title = "Buy groceries", isCompleted = false),
            TaskItem(id = 2, title = "Read Compose documentation", isCompleted = true),
            TaskItem(id = 3, title = "Implement custom Saver", isCompleted = false)
        )
    }

    DemoCard(label = "Task List (Modifying Items in mutableStateListOf)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Rule: To update an element, replace it at its index using .copy()!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            tasks.forEachIndexed { index, task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { isChecked ->
                                // ✅ CORRECT: Replace the item in the list with a new copy
                                tasks[index] = task.copy(isCompleted = isChecked)
                            }
                        )
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { tasks.removeAt(index) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                    tasks.add(TaskItem(id = nextId, title = "New Task #$nextId"))
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(" Add New Task")
            }
        }
    }
}

// ─── SECTION 3: MUTABLE STATE MAP ────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StateMapDemoCard() {
    // ✅ mutableStateMapOf tracks key-value entries
    val userScores = remember {
        mutableStateMapOf(
            "Alice" to 85,
            "Bob" to 92,
            "Charlie" to 78
        )
    }

    DemoCard(label = "mutableStateMapOf (Key-Value Leaderboard)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Entries in Map: ${userScores.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                userScores.forEach { (name, score) ->
                    InputChip(
                        selected = true,
                        onClick = {
                            // Increment score for this specific key
                            userScores[name] = score + 5
                        },
                        label = { Text("$name: $score pts") },
                        trailingIcon = {
                            IconButton(
                                onClick = { userScores.remove(name) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove")
                            }
                        }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val names = listOf("Dave", "Emma", "Frank", "Grace", "Henry")
                        val unused = names.firstOrNull { it !in userScores }
                        if (unused != null) {
                            userScores[unused] = 70
                        }
                    },
                    enabled = userScores.size < 6
                ) {
                    Text("+ Add Player")
                }

                OutlinedButton(
                    onClick = {
                        userScores.clear()
                        userScores["Alice"] = 85
                        userScores["Bob"] = 92
                        userScores["Charlie"] = 78
                    }
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Text(" Reset")
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
