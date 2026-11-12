package android.ai.jetpackdemonstration.ui.components.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DialogDemoScreen(modifier: Modifier = Modifier) {
    var showAlertDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("No dialog action yet") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(status, style = MaterialTheme.typography.titleMedium)

        Button(onClick = { showAlertDialog = true }) {
            Text("Show AlertDialog")
        }

        OutlinedButton(onClick = { showCustomDialog = true }) {
            Text("Show Custom Dialog")
        }
    }

    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to proceed with this action?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        status = "AlertDialog confirmed"
                        showAlertDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        status = "AlertDialog dismissed"
                        showAlertDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomDialog) {
        Dialog(onDismissRequest = { showCustomDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Custom Dialog", style = MaterialTheme.typography.titleLarge)
                    Text("Built with Dialog + Card")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                status = "Custom Dialog closed"
                                showCustomDialog = false
                            }
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetDemoScreen(modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("None") }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selected: $selectedItem",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = { showBottomSheet = true }) {
                Text("Show ModalBottomSheet")
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                listOf("Share", "Copy Link", "Bookmark", "Download").forEach { option ->
                    ListItem(
                        headlineContent = { Text(option) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Button(
                    onClick = {
                        selectedItem = "Closed via button"
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                ) {
                    Text("Hide Sheet")
                }
            }
        }
    }
}

@Composable
fun SnackbarDemoScreen(modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var lastResult by remember { mutableStateOf("Waiting") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Result: $lastResult", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Item archived",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        lastResult = when (result) {
                            SnackbarResult.ActionPerformed -> "Undo clicked"
                            SnackbarResult.Dismissed -> "Dismissed"
                        }
                    }
                }
            ) {
                Text("Show Snackbar with Action")
            }
        }
    }
}

@Composable
fun ProgressIndicatorDemoScreen(modifier: Modifier = Modifier) {
    var progress by remember { mutableFloatStateOf(0.4f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Determinate (${(progress * 100).toInt()}%)")
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        CircularProgressIndicator(progress = { progress })

        Slider(
            value = progress,
            onValueChange = { progress = it }
        )

        Spacer(Modifier.height(8.dp))
        Text("Indeterminate")
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        CircularProgressIndicator()
    }
}

@Composable
fun BadgeDemoScreen(modifier: Modifier = Modifier) {
    var cartCount by remember { mutableIntStateOf(3) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = { Badge() }
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
            }

            BadgedBox(
                badge = {
                    if (cartCount > 0) {
                        Badge { Text("$cartCount") }
                    }
                }
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
            }

            BadgedBox(
                badge = {
                    Badge { Text("99+") }
                }
            ) {
                Icon(Icons.Filled.Email, contentDescription = "Inbox")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { if (cartCount > 0) cartCount-- }) {
                Text("-1")
            }
            Button(onClick = { cartCount++ }) {
                Text("+1")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipDemoScreen(modifier: Modifier = Modifier) {
    val richTooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Long-press icons to view tooltips", style = MaterialTheme.typography.titleMedium)

        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above
            ),
            tooltip = {
                PlainTooltip { Text("Add to favorites") }
            },
            state = rememberTooltipState()
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
            }
        }

        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Below
            ),
            tooltip = {
                RichTooltip(
                    title = { Text("Rich Tooltip") },
                    action = {
                        TextButton(onClick = { scope.launch { richTooltipState.dismiss() } }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text("Rich tooltips support a title, body text, and an action.")
                }
            },
            state = richTooltipState
        ) {
            IconButton(onClick = { scope.launch { richTooltipState.show() } }) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshDemoScreen(modifier: Modifier = Modifier) {
    var refreshCount by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val items = remember(refreshCount) {
        (1..20).map { "Item #$it (Refresh $refreshCount)" }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                delay(1200)
                refreshCount++
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items, key = { it }) { item ->
                ListItem(headlineContent = { Text(item) })
            }
        }
    }
}
