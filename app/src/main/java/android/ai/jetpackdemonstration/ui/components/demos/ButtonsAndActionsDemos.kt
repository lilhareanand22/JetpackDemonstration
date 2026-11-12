package android.ai.jetpackdemonstration.ui.components.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonDemoScreen(modifier: Modifier = Modifier) {
    var clickCount by remember { mutableIntStateOf(0) }
    var enabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clicks: $clickCount",
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled")
                Spacer(Modifier.width(8.dp))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
        }

        Button(
            onClick = { clickCount++ },
            enabled = enabled
        ) {
            Text("Filled Button")
        }

        FilledTonalButton(
            onClick = { clickCount++ },
            enabled = enabled
        ) {
            Text("Filled Tonal Button")
        }

        ElevatedButton(
            onClick = { clickCount++ },
            enabled = enabled
        ) {
            Text("Elevated Button")
        }

        OutlinedButton(
            onClick = { clickCount++ },
            enabled = enabled
        ) {
            Text("Outlined Button")
        }

        TextButton(
            onClick = { clickCount++ },
            enabled = enabled
        ) {
            Text("Text Button")
        }
    }
}

@Composable
fun IconButtonDemoScreen(modifier: Modifier = Modifier) {
    var isFavorite by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("None") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Last action: $lastAction",
            style = MaterialTheme.typography.titleMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = { lastAction = "Standard IconButton" }) {
                Icon(Icons.Rounded.Share, contentDescription = "Share")
            }

            FilledIconButton(onClick = { lastAction = "FilledIconButton" }) {
                Icon(Icons.Rounded.Bookmark, contentDescription = "Bookmark")
            }

            FilledTonalIconButton(onClick = { lastAction = "FilledTonalIconButton" }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }

            OutlinedIconButton(onClick = { lastAction = "OutlinedIconButton" }) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = {
                    isFavorite = it
                    lastAction = if (it) "Favorited" else "Unfavorited"
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite"
                )
            }
            Text(if (isFavorite) "Favorite: On" else "Favorite: Off")
        }
    }
}

@Composable
fun FloatingActionButtonDemoScreen(modifier: Modifier = Modifier) {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FAB Taps: $count",
            style = MaterialTheme.typography.titleMedium
        )

        SmallFloatingActionButton(onClick = { count++ }) {
            Icon(Icons.Filled.Add, contentDescription = "Small FAB")
        }

        FloatingActionButton(onClick = { count++ }) {
            Icon(Icons.Filled.Add, contentDescription = "Standard FAB")
        }

        LargeFloatingActionButton(onClick = { count++ }) {
            Icon(Icons.Filled.Add, contentDescription = "Large FAB")
        }

        ExtendedFloatingActionButton(
            onClick = { count++ },
            icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            text = { Text("Extended FAB") }
        )
    }
}

@Composable
fun SegmentedButtonDemoScreen(modifier: Modifier = Modifier) {
    val singleOptions = listOf("Day", "Week", "Month")
    var selectedIndex by remember { mutableIntStateOf(0) }

    val multiOptions = listOf("Walk", "Bike", "Drive")
    val selectedMulti = remember { mutableStateListOf(false, true, false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Single Choice: ${singleOptions[selectedIndex]}",
            style = MaterialTheme.typography.titleMedium
        )

        SingleChoiceSegmentedButtonRow {
            singleOptions.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = singleOptions.size
                    ),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex
                ) {
                    Text(label)
                }
            }
        }

        val activeMulti = multiOptions.filterIndexed { index, _ -> selectedMulti[index] }
        Text(
            text = "Multi Choice: ${activeMulti.ifEmpty { listOf("None") }.joinToString()}",
            style = MaterialTheme.typography.titleMedium
        )

        MultiChoiceSegmentedButtonRow {
            multiOptions.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = multiOptions.size
                    ),
                    checked = selectedMulti[index],
                    onCheckedChange = { selectedMulti[index] = it }
                ) {
                    Text(label)
                }
            }
        }
    }
}
