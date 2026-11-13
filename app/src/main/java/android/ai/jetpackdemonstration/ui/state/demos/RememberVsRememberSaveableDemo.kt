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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─── DATA CLASS FOR CUSTOM SAVER DEMO ─────────────────────────────────

data class UserProfile(
    val username: String,
    val level: Int
)

// 1. Custom Saver using listSaver
val UserProfileListSaver: Saver<UserProfile, Any> = listSaver(
    save = { listOf(it.username, it.level) },
    restore = { UserProfile(username = it[0] as String, level = it[1] as Int) }
)

// 2. Custom Saver using mapSaver
val UserProfileMapSaver: Saver<UserProfile, Any> = mapSaver(
    save = { mapOf("username" to it.username, "level" to it.level) },
    restore = {
        UserProfile(
            username = it["username"] as String,
            level = it["level"] as Int
        )
    }
)

// ─── MAIN DEMO SCREEN ────────────────────────────────────────────────

@Composable
fun RememberVsRememberSaveableDemo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── SECTION 1: SIDE-BY-SIDE ROTATION TEST ──
        SectionHeader(
            number = "1",
            title = "Rotation Survival Test",
            subtitle = "Increment both counters and rotate the device (or trigger config change). Watch what happens!"
        )
        RotationComparisonCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 2: TEXT INPUT COMPARISON ──
        SectionHeader(
            number = "2",
            title = "Text Field Survival Comparison",
            subtitle = "Type into both fields, then rotate the device or navigate away and back."
        )
        TextFieldComparisonCard()

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ── SECTION 3: CUSTOM DATA CLASS WITH SAVER ──
        SectionHeader(
            number = "3",
            title = "Custom Object Saver (listSaver & mapSaver)",
            subtitle = "Classes not supported by default Bundle types require a custom Saver for rememberSaveable."
        )
        CustomSaverCard()
    }
}

// ─── SECTION 1: SIDE-BY-SIDE ROTATION COMPARISON ─────────────────────

@Composable
private fun RotationComparisonCard() {
    // ❌ remember: Survives recomposition ONLY. Resets to 0 on configuration change / process restart.
    var rememberCount by remember { mutableIntStateOf(0) }

    // ✅ rememberSaveable: Survives recomposition AND configuration changes via Android SavedState (Bundle).
    var saveableCount by rememberSaveable { mutableIntStateOf(0) }

    DemoCard(label = "Side-by-Side Counter Comparison") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "remember",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "$rememberCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lost on rotation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { rememberCount++ }) {
                    Text("+1")
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "rememberSaveable",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$saveableCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Survives rotation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { saveableCount++ }) {
                    Text("+1")
                }
            }
        }
    }
}

// ─── SECTION 2: TEXT INPUT PERSISTENCE ────────────────────────────────

@Composable
private fun TextFieldComparisonCard() {
    var transientText by remember { mutableStateOf("") }
    var persistentText by rememberSaveable { mutableStateOf("") }

    DemoCard(label = "Form Input Persistence") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = transientText,
                onValueChange = { transientText = it },
                label = { Text("remember (Lost on rotate)") },
                placeholder = { Text("Type something here...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = persistentText,
                onValueChange = { persistentText = it },
                label = { Text("rememberSaveable (Preserved on rotate)") },
                placeholder = { Text("Type something here...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── SECTION 3: CUSTOM SAVER IMPLEMENTATION ──────────────────────────

@Composable
private fun CustomSaverCard() {
    // Custom object saved with listSaver
    var profile by rememberSaveable(stateSaver = UserProfileListSaver) {
        mutableStateOf(UserProfile(username = "AndroidDev", level = 1))
    }

    DemoCard(label = "UserProfile Custom Saver (listSaver)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User: ${profile.username}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Level: ${profile.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        if (profile.level > 1) {
                            profile = profile.copy(level = profile.level - 1)
                        }
                    }) {
                        Text("- Lv")
                    }
                    Button(onClick = {
                        profile = profile.copy(level = profile.level + 1)
                    }) {
                        Text("+ Lv")
                    }
                }
            }

            OutlinedTextField(
                value = profile.username,
                onValueChange = { profile = profile.copy(username = it) },
                label = { Text("Edit username (persisted across rotation)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
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
