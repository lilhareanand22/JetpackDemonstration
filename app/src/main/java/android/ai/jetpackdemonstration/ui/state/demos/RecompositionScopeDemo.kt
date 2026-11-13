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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// ─── RECOMPOSITION COUNTER HELPER ────────────────────────────────────

class RecomposeCounter {
    var count: Int = 0
        private set

    fun increment(): Int {
        count++
        return count
    }
}

// ─── MAIN DEMO SCREEN ────────────────────────────────────────────────

@Composable
fun RecompositionScopeDemo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── SECTION 1: PARENT VS. ISOLATED CHILD SCOPE ──
        SectionHeader(
            number = "1",
            title = "Recomposition Scoping (Localized Invalidation)",
            subtitle = "Compose only recomposes the nearest scope reading the state. Child counter updates without invalidating parent!"
        )
        ScopingDemoCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 2: DEFERRED STATE READS (OFFSET) ──
        SectionHeader(
            number = "2",
            title = "Deferred Reads via Lambda: Modifier.offset",
            subtitle = "Direct read triggers full Composition on every slider pixel. Lambda offset { } skips Composition entirely and executes in Layout phase!"
        )
        DeferredOffsetDemoCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 3: DEFERRED DRAW PHASE (drawBehind) ──
        SectionHeader(
            number = "3",
            title = "Draw-Phase State Reads (Modifier.drawBehind)",
            subtitle = "Passing state into drawBehind skips Composition & Layout, executing strictly in the Draw phase!"
        )
        DeferredDrawDemoCard()
    }
}

// ─── SECTION 1: RECOMPOSITION SCOPING DEMO ───────────────────────────

@Composable
private fun ScopingDemoCard() {
    // Parent state
    var parentCounter by remember { mutableIntStateOf(0) }
    // Separate state only read by Child B
    var isolatedChildCounter by remember { mutableIntStateOf(0) }

    // Recomposition trackers
    val parentRecompositions = remember { RecomposeCounter() }
    SideEffect {
        parentRecompositions.increment()
    }

    DemoCard(label = "Parent Composable (Recomposed: ${parentRecompositions.count} times)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Parent Counter: $parentCounter",
                style = MaterialTheme.typography.titleMedium
            )

            Button(onClick = { parentCounter++ }) {
                Text("+ Parent Counter (Recomposes Parent + Children)")
            }

            Spacer(Modifier.height(4.dp))

            // Child A: Reads parent counter passed as parameter
            ChildWithParam(
                label = "Child A (Reads Parent Counter)",
                value = parentCounter
            )

            // Child B: Reads its OWN isolated state — parent does NOT read this!
            IsolatedChild(
                count = isolatedChildCounter,
                onIncrement = { isolatedChildCounter++ }
            )
        }
    }
}

@Composable
private fun ChildWithParam(label: String, value: Int) {
    val childRecompositions = remember { RecomposeCounter() }
    SideEffect {
        childRecompositions.increment()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(text = "Received Value: $value", style = MaterialTheme.typography.bodySmall)
        Text(
            text = "Recomposed: ${childRecompositions.count} times",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun IsolatedChild(count: Int, onIncrement: () -> Unit) {
    val childRecompositions = remember { RecomposeCounter() }
    SideEffect {
        childRecompositions.increment()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Child B (Isolated State Scope)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Child Count: $count", style = MaterialTheme.typography.bodySmall)
        Text(
            text = "Recomposed: ${childRecompositions.count} times",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Button(onClick = onIncrement) {
            Text("+ Child Counter (Parent skips recomposition!)")
        }
    }
}

// ─── SECTION 2: DEFERRED OFFSET DEMO ─────────────────────────────────

@Composable
private fun DeferredOffsetDemoCard() {
    var offsetX by remember { mutableFloatStateOf(0f) }

    // Recomposition counter for this container
    val containerRecompositions = remember { RecomposeCounter() }
    SideEffect {
        containerRecompositions.increment()
    }

    DemoCard(label = "Deferred Reads via Lambda (Container Recomposed: ${containerRecompositions.count} times)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Drag the slider to animate the boxes below:",
                style = MaterialTheme.typography.bodySmall
            )

            Slider(
                value = offsetX,
                onValueChange = { offsetX = it },
                valueRange = 0f..150f
            )

            // Comparison Row
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Box 1: Eager Read in Composition Phase (Causes full recomposition)
                NonDeferredBox(offsetX = offsetX)

                // Box 2: Deferred Read in Layout Phase (Zero recomposition!)
                DeferredBox(offsetProvider = { offsetX })
            }
        }
    }
}

@Composable
private fun NonDeferredBox(offsetX: Float) {
    val recompositions = remember { RecomposeCounter() }
    SideEffect {
        recompositions.increment()
    }

    Column {
        Text(
            text = "❌ Direct Read (Modifier.offset(x.dp)): Recomposed ${recompositions.count} times",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .offset(x = offsetX.dp) // Read happens during Composition!
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.error)
        )
    }
}

@Composable
private fun DeferredBox(offsetProvider: () -> Float) {
    val recompositions = remember { RecomposeCounter() }
    SideEffect {
        recompositions.increment()
    }

    Column {
        Text(
            text = "✅ Lambda Read (Modifier.offset { IntOffset(...) }): Recomposed ${recompositions.count} times",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                // Read happens during Layout phase inside the lambda — Composition is skipped!
                .offset { IntOffset(x = offsetProvider().roundToInt() * 3, y = 0) }
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

// ─── SECTION 3: DEFERRED DRAW PHASE (drawBehind) ─────────────────────

@Composable
private fun DeferredDrawDemoCard() {
    var colorHue by remember { mutableFloatStateOf(0f) }

    val drawBoxRecompositions = remember { RecomposeCounter() }
    SideEffect {
        drawBoxRecompositions.increment()
    }

    DemoCard(label = "Draw-Phase State Reads (Recomposed: ${drawBoxRecompositions.count} times)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Slider updates color hue at 60fps without triggering Recomposition or Layout phases!",
                style = MaterialTheme.typography.bodySmall
            )

            Slider(
                value = colorHue,
                onValueChange = { colorHue = it },
                valueRange = 0f..360f
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    // ✅ drawBehind executes in the Draw phase.
                    // Reading colorHue here invalidates ONLY the Draw phase!
                    .drawBehind {
                        drawRect(
                            color = Color.hsl(hue = colorHue, saturation = 0.7f, lightness = 0.5f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hue: ${colorHue.roundToInt()}° (Draw Phase Only)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
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
