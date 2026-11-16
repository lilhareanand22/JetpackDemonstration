package android.ai.jetpackdemonstration.ui.state.demos

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DerivedStateDemo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── SECTION 1: SCROLL THRESHOLD OPTIMIZATION (THE CLASSIC EXAMPLE) ──
        SectionHeader(
            number = "1",
            title = "Scroll Threshold: Direct Calculation vs. derivedStateOf",
            subtitle = "Scroll the row below. Direct calculation recomposes on every single item. derivedStateOf recomposes ONLY when threshold flips!"
        )
        ScrollOptimizationCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 2: EXPENSIVE FILTERING / STRING VALIDATION ──
        SectionHeader(
            number = "2",
            title = "Filtered / Validated State Calculation",
            subtitle = "Computing form validity or filtering a list where derived value updates much less often than the raw inputs."
        )
        FormValidationCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── SECTION 3: WHEN NOT TO USE derivedStateOf ──
        SectionHeader(
            number = "3",
            title = "The Golden Rule: When NOT to Use derivedStateOf",
            subtitle = "If the derived state changes at the EXACT SAME rate as the input (e.g., fullName = first + last), derivedStateOf adds pure overhead!"
        )
        WhenNotToUseCard()
    }
}

// ─── SECTION 1: SCROLL OPTIMIZATION COMPARISON ───────────────────────

@Composable
private fun ScrollOptimizationCard() {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    DemoCard(label = "Side-by-Side Recomposition Comparison") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Scroll the horizontal list. Watch how derivedStateOf protects its composable from recomposition storm:",
                style = MaterialTheme.typography.bodySmall
            )

            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(30) { index ->
                    Card(
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("#$index", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { scope.launch { listState.animateScrollToItem(10) } }
                ) {
                    Text("Scroll to #10")
                }
                Button(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } }
                ) {
                    Text("Scroll to #0")
                }
            }

            // Box A: Direct read inside its OWN isolated scope
            // Reads listState.firstVisibleItemIndex -> recomposes on EVERY item change!
            DirectReadChildBox(listState = listState)

            // Box B: Derived state read inside its OWN isolated scope
            // Reads derivedStateOf { index > 5 } -> recomposes ONLY when condition flips!
            DerivedReadChildBox(listState = listState)
        }
    }
}

/**
 * ❌ Direct Read Box:
 * Reads `listState.firstVisibleItemIndex` directly during its own composition.
 * As you scroll from 0 to 10, this composable recomposes on EVERY SINGLE index step (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)!
 */
@Composable
private fun DirectReadChildBox(listState: androidx.compose.foundation.lazy.LazyListState) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    SideEffect { recomposeCount++ }

    // Direct read: registers a dependency on `firstVisibleItemIndex`
    val currentIndex = listState.firstVisibleItemIndex
    val isPastIndex5 = currentIndex > 5

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = "❌ Direct Read (currentIndex = $currentIndex, Past #5: $isPastIndex5)",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Recomposed: $recomposeCount times (recomposes on every single item!)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * ✅ with derivedStateOf Box:
 * Uses `derivedStateOf { listState.firstVisibleItemIndex > 5 }`.
 * When scrolling from 0 to 10, `derivedStateOf` evaluates the condition, but because
 * it evaluates to `false` (0..5) and then `true` (6..10), this composable recomposes
 * ONLY ONCE when the condition flips across the threshold!
 */
@Composable
private fun DerivedReadChildBox(listState: androidx.compose.foundation.lazy.LazyListState) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    SideEffect { recomposeCount++ }

    // Derived state: emits an update ONLY when the boolean condition flips!
    val isPastIndex5 by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = "✅ with derivedStateOf (Past #5: $isPastIndex5)",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Recomposed: $recomposeCount times (recomposes ONLY when threshold flips!)",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ─── SECTION 2: FORM VALIDATION DEMO ─────────────────────────────────

@Composable
private fun FormValidationCard() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ✅ derivedStateOf: Recomputed on each keystroke, but triggers recomposition
    // of the Submit button ONLY when `isValid` transitions between false <-> true!
    val isFormValid by remember {
        derivedStateOf {
            username.length >= 4 && password.length >= 6
        }
    }

    DemoCard(label = "Form Validity Optimization") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Validation rule: Username >= 4 chars, Password >= 6 chars.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username (min 4)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 6)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {},
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isFormValid) "Submit (Form is Valid)" else "Incomplete Form")
            }
        }
    }
}

// ─── SECTION 3: WHEN NOT TO USE derivedStateOf ────────────────────────

@Composable
private fun WhenNotToUseCard() {
    var firstName by remember { mutableStateOf("John") }
    var lastName by remember { mutableStateOf("Doe") }

    // ❌ ANTI-PATTERN: Both inputs change at 1:1 rate with the output.
    // derivedStateOf adds allocation & snapshot observer overhead with ZERO benefit!
    // val fullName by remember { derivedStateOf { "$firstName $lastName" } }

    // ✅ CORRECT: Just use simple String concatenation!
    val fullName = "$firstName $lastName"

    DemoCard(label = "Anti-Pattern vs Correct Usage") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Golden Rule: Use derivedStateOf ONLY when the derived output changes LESS FREQUENTLY than the input states!",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "• Input changes frequently (e.g. scroll pixels, keystrokes)\n• Output is boolean or threshold (e.g. scroll > 5, isValid)\n➔ USE derivedStateOf ✅",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• Input and output change at 1:1 rate (e.g. fullName = firstName + lastName)\n➔ DO NOT USE derivedStateOf (Adds pure overhead) ❌",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text("Computed Full Name: $fullName", fontWeight = FontWeight.SemiBold)
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
