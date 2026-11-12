package android.ai.jetpackdemonstration.ui.components.demos

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun TextFieldDemoScreen(modifier: Modifier = Modifier) {
    var filledText by remember { mutableStateOf("") }
    var outlinedText by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var basicText by remember { mutableStateOf("BasicTextField") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextField(
            value = filledText,
            onValueChange = { filledText = it },
            label = { Text("Filled TextField") },
            placeholder = { Text("Enter name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = outlinedText,
            onValueChange = { outlinedText = it },
            label = { Text("OutlinedTextField") },
            supportingText = { Text("Required: ${outlinedText.length}/20") },
            isError = outlinedText.length > 20,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle password"
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        BasicTextField(
            value = basicText,
            onValueChange = { basicText = it },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarDemoScreen(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val items = remember {
        listOf("Button", "Card", "Checkbox", "Chip", "Dialog", "ListItem", "Slider", "Switch", "Tabs")
    }
    val filtered = items.filter { it.contains(query, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search components") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            filtered.forEach { item ->
                ListItem(
                    headlineContent = { Text(item) },
                    modifier = Modifier.clickable {
                        query = item
                        expanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "Selected query: ${query.ifEmpty { "None" }}",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun CheckboxDemoScreen(modifier: Modifier = Modifier) {
    val childStates = remember { mutableStateListOf(true, false, false) }
    val parentState = when {
        childStates.all { it } -> ToggleableState.On
        childStates.none { it } -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriStateCheckbox(
                state = parentState,
                onClick = {
                    val newState = parentState != ToggleableState.On
                    childStates.indices.forEach { childStates[it] = newState }
                }
            )
            Text("Select All", style = MaterialTheme.typography.titleMedium)
        }

        childStates.forEachIndexed { index, checked ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 24.dp)
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { childStates[index] = it }
                )
                Text("Option ${index + 1}")
            }
        }
    }
}

@Composable
fun RadioButtonDemoScreen(modifier: Modifier = Modifier) {
    val options = listOf("Calls", "Missed", "Voicemail")
    var selectedOption by remember { mutableStateOf(options[0]) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Selected: $selectedOption",
            style = MaterialTheme.typography.titleMedium
        )

        options.forEach { text ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { selectedOption = text },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SwitchDemoScreen(modifier: Modifier = Modifier) {
    var basicChecked by remember { mutableStateOf(true) }
    var iconChecked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Standard Switch")
            Switch(
                checked = basicChecked,
                onCheckedChange = { basicChecked = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Switch with Thumb Icon")
            Switch(
                checked = iconChecked,
                onCheckedChange = { iconChecked = it },
                thumbContent = if (iconChecked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun SliderDemoScreen(modifier: Modifier = Modifier) {
    var continuousValue by remember { mutableFloatStateOf(0.5f) }
    var steppedValue by remember { mutableFloatStateOf(20f) }
    var rangeValues by remember { mutableStateOf(20f..80f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text("Continuous: ${(continuousValue * 100).roundToInt()}%")
            Slider(
                value = continuousValue,
                onValueChange = { continuousValue = it }
            )
        }

        Column {
            Text("Stepped (0..100): ${steppedValue.roundToInt()}")
            Slider(
                value = steppedValue,
                onValueChange = { steppedValue = it },
                valueRange = 0f..100f,
                steps = 4
            )
        }

        Column {
            Text("RangeSlider: ${rangeValues.start.roundToInt()} .. ${rangeValues.endInclusive.roundToInt()}")
            RangeSlider(
                value = rangeValues,
                onValueChange = { rangeValues = it },
                valueRange = 0f..100f
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipDemoScreen(modifier: Modifier = Modifier) {
    var filterSelected by remember { mutableStateOf(true) }
    var showInputChip by remember { mutableStateOf(true) }
    var chipMessage by remember { mutableStateOf("Tap a chip") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(chipMessage, style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(
                onClick = { chipMessage = "AssistChip tapped" },
                label = { Text("AssistChip") },
                leadingIcon = {
                    Icon(Icons.Filled.Settings, contentDescription = null)
                }
            )

            FilterChip(
                selected = filterSelected,
                onClick = {
                    filterSelected = !filterSelected
                    chipMessage = "FilterChip: $filterSelected"
                },
                label = { Text("FilterChip") },
                leadingIcon = if (filterSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )

            if (showInputChip) {
                InputChip(
                    selected = true,
                    onClick = {
                        showInputChip = false
                        chipMessage = "InputChip dismissed"
                    },
                    label = { Text("InputChip") },
                    avatar = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(InputChipDefaults.IconSize)
                        )
                    }
                )
            } else {
                Button(onClick = { showInputChip = true }) {
                    Text("Restore InputChip")
                }
            }

            SuggestionChip(
                onClick = { chipMessage = "SuggestionChip tapped" },
                label = { Text("SuggestionChip") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenusDemoScreen(modifier: Modifier = Modifier) {
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf("None") }

    val options = listOf("Kotlin", "Java", "Compose", "Coroutines")
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("DropdownMenu action: $selectedAction")
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Open Menu")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            selectedAction = "Edit"
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            selectedAction = "Duplicate"
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            selectedAction = "Delete"
                            menuExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("ExposedDropdownMenuBox") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}
