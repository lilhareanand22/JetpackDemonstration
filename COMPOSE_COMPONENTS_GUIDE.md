# Jetpack Compose Components — Study Guide & Top 20 Interview Questions

## 1. App Architecture (Tab 1: Components)

### Navigation & Screen Flow
1. **Entry Point (`MainActivity.kt`)**: Calls `enableEdgeToEdge()` and sets `JetpackDemonstrationTheme { JetpackDemoApp() }`.
2. **Type-Safe Routes (`ui/navigation/AppDestinations.kt`)**:
   - Uses `@Serializable` objects/classes (`ComponentsRoute`, `StateRoute`, `SideEffectsRoute`, `ComponentDetailRoute(val componentId: String)`).
3. **App Shell (`ui/navigation/AppNavHost.kt`)**:
   - `Scaffold` hosts the `NavigationBar` with 3 top-level tabs.
   - `showBottomBar` checks `currentDestination?.hasRoute(...)` so the bottom bar is visible on the 3 main tabs and hidden on `ComponentDetailRoute`.
4. **Catalog & Search (`ui/components/ComponentCatalog.kt` & `ComponentsScreen.kt`)**:
   - `ComponentCatalog` holds all 34+ components grouped into 7 `ComponentCategory` sections.
   - `ComponentsScreen` stores `searchQuery` in `rememberSaveable`, filters items, and hoists taps via `onComponentClick(componentId)`.
5. **Detail Host (`ui/components/ComponentDetailScreen.kt`)**:
   - Receives `componentId`, displays a `TopAppBar` with back navigation (`popBackStack()`), and renders only the live Compose demo for that component.

---

## 2. Implemented Component Categories

| Category | Demo File | Components Covered |
| :--- | :--- | :--- |
| **Buttons & Actions** | `ButtonsAndActionsDemos.kt` | `Button`, `FilledTonalButton`, `ElevatedButton`, `OutlinedButton`, `TextButton`, `IconButton`, `IconToggleButton`, `FloatingActionButton` (Small, Standard, Large, Extended), `SingleChoiceSegmentedButtonRow`, `MultiChoiceSegmentedButtonRow` |
| **App Bars & Navigation** | `AppBarsAndNavDemos.kt` | `TopAppBar` (Small, CenterAligned, Medium, Large), `BottomAppBar`, `NavigationBar`, `NavigationRail`, `ModalNavigationDrawer`, `PrimaryTabRow`, `SecondaryTabRow`, `PrimaryScrollableTabRow`, `Scaffold` |
| **Cards, Lists & Dividers** | `CardsAndListsDemos.kt` | `Card`, `ElevatedCard`, `OutlinedCard`, `ListItem`, `HorizontalDivider`, `VerticalDivider`, `LazyColumn`, `LazyRow`, `LazyVerticalGrid`, `HorizontalPager`, `VerticalPager`, `HorizontalMultiBrowseCarousel`, `HorizontalUncontainedCarousel` |
| **Inputs & Selection** | `InputsAndSelectionDemos.kt` | `TextField`, `OutlinedTextField`, `BasicTextField`, `SearchBar`, `Checkbox`, `TriStateCheckbox`, `RadioButton`, `Switch`, `Slider`, `RangeSlider`, `AssistChip`, `FilterChip`, `InputChip`, `SuggestionChip`, `DropdownMenu`, `ExposedDropdownMenuBox` |
| **Dialogs & Feedback** | `DialogsAndFeedbackDemos.kt` | `AlertDialog`, `Dialog`, `ModalBottomSheet`, `Snackbar` (`SnackbarHostState`), `LinearProgressIndicator`, `CircularProgressIndicator`, `Badge`, `BadgedBox`, `TooltipBox` (`PlainTooltip`, `RichTooltip`), `PullToRefreshBox` |
| **Date & Time Pickers** | `DateAndTimeDemos.kt` | `DatePicker`, `DatePickerDialog`, `DateRangePicker`, `TimePicker`, `TimeInput` |
| **Layouts, Text & Media** | `LayoutsAndCoreDemos.kt` | `Text`, `buildAnnotatedString`, `SelectionContainer`, `Image` (`ContentScale`), `Icon`, `Box`, `Row`, `Column`, `FlowRow`, `FlowColumn`, `Surface` |

---

## 3. Top 20 Jetpack Compose Components Interview Questions

### 1. How does Jetpack Compose differ from the traditional Android View (XML) system?
Compose is **declarative**: you describe *what* the UI should look like for a given state, and Compose automatically updates the UI when state changes. Traditional XML Views are **imperative**: you manually find views (`findViewById` / ViewBinding) and mutate their properties over time.

### 2. What is a Slot API in Jetpack Compose, and why is `Scaffold` a good example?
A Slot API accepts `@Composable () -> Unit` lambda parameters so callers can plug in arbitrary UI into predefined positions. `Scaffold` exposes slots like `topBar`, `bottomBar`, `floatingActionButton`, `snackbarHost`, and `content`, handling Material layout spacing and `innerPadding` automatically.

### 3. Why must you apply `innerPadding` passed by `Scaffold` to its `content`?
`Scaffold` calculates the insets occupied by `topBar`, `bottomBar`, and system bars and passes them as `PaddingValues`. Ignoring `innerPadding` causes content to overlap or get hidden behind the app bars.

### 4. Why does `Modifier` order matter in Jetpack Compose?
Modifiers are applied sequentially from outside to inside (each modifier wraps the next). For example, `Modifier.padding(16.dp).background(Color.Red)` adds outer margin before drawing the red background, whereas `Modifier.background(Color.Red).padding(16.dp)` paints the red background first and adds inner padding.

### 5. What is the difference between `Column` with `verticalScroll` and `LazyColumn`?
`Column(Modifier.verticalScroll(...))` composes and measures **all** children upfront, which is only suitable for small, fixed lists. `LazyColumn` composes and lays out only the items currently visible in the viewport (plus a small prefetch buffer), making it efficient for large or dynamic datasets.

### 6. Why should you provide a `key` in `LazyColumn` / `LazyRow` `items(...)`?
Without a `key`, Compose identifies items by their list index. If items are inserted, removed, or reordered, positional keys cause unnecessary recompositions and broken item-level state. A stable unique `key = { item.id }` preserves each item's composition and state across list changes.

### 7. How do `Box`, `Row`, and `Column` scopes work?
Each layout passes a receiver scope (`BoxScope`, `RowScope`, `ColumnScope`) to its content lambda. That is why `Modifier.align(Alignment.BottomEnd)` is only available inside `Box`, and `Modifier.weight(1f)` is only available inside `Row` or `Column`.

### 8. When should you use `FlowRow` / `FlowColumn` instead of `Row` / `Column`?
`Row` and `Column` place children in a single line and overflow or clip if they don't fit. `FlowRow` and `FlowColumn` automatically wrap children onto the next row or column when the main-axis space is exhausted (ideal for tag clouds or chip groups).

### 9. What is the difference between `tonalElevation` and `shadowElevation` on `Surface`?
In Material 3, `tonalElevation` applies a color overlay derived from `surfaceTint` / primary tone to indicate elevation visually, whereas `shadowElevation` draws a physical drop shadow around the `Surface`.

### 10. How does `TopAppBarDefaults.exitUntilCollapsedScrollBehavior()` work with a scrollable list?
The `scrollBehavior` provides a `nestedScrollConnection` attached to the parent container via `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`. As the `LazyColumn` scrolls, scroll deltas flow through the nested scroll chain, collapsing or expanding `MediumTopAppBar` / `LargeTopAppBar`.

### 11. Why are `TextField` and `OutlinedTextField` called "controlled" components?
They do not own the text string themselves; they display the `value` passed in and emit user edits via `onValueChange`. If `onValueChange` does not update the backing state, the displayed text will not change.

### 12. When would you use `BasicTextField` instead of `TextField` or `OutlinedTextField`?
`BasicTextField` provides raw text editing without Material decoration (no label, underline, outline, or minimum padding), making it ideal for custom-styled inputs, code editors, or compact inline search fields.

### 13. What is the difference between `DropdownMenu` and `ExposedDropdownMenuBox`?
`DropdownMenu` is a general-purpose popup menu anchored to any composable (like an overflow `IconButton`). `ExposedDropdownMenuBox` is a specialized Material container that pairs a text field (`menuAnchor`) with an `ExposedDropdownMenu` sized to match the field's width for form dropdowns.

### 14. How do `TriStateCheckbox` and `Checkbox` differ?
`Checkbox` accepts a `Boolean` (`checked = true/false`). `TriStateCheckbox` accepts `ToggleableState` (`On`, `Off`, `Indeterminate`), which is standard for "Select All" parent checkboxes when only some child items are checked.

### 15. What is the accessible way to build a `RadioButton` row in Compose?
Apply `Modifier.selectableGroup()` to the parent container and `Modifier.selectable(selected = ..., onClick = ..., role = Role.RadioButton)` to the entire `Row`, while passing `onClick = null` to the `RadioButton` itself. This makes the whole row tappable and exposes proper radio-group semantics to screen readers.

### 16. How are `AlertDialog` and `Dialog` shown and hidden in Compose?
Declaratively using state: `if (showDialog) { AlertDialog(onDismissRequest = { showDialog = false }, ...) }`. Unlike imperative `DialogFragment.show()`, removing the composable from composition dismisses it cleanly.

### 17. Why should `sheetState.hide()` be called inside a coroutine before setting `showBottomSheet = false`?
Calling `showBottomSheet = false` immediately removes `ModalBottomSheet` from composition without a slide-down animation. Running `scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false }` plays the hide animation smoothly first.

### 18. How does `SnackbarHostState.showSnackbar(...)` handle user actions like "Undo"?
`showSnackbar` is a `suspend` function that waits until the snackbar is dismissed or its action button is clicked, returning `SnackbarResult.ActionPerformed` or `SnackbarResult.Dismissed` directly to the caller coroutine.

### 19. Why do Material 3 determinate progress indicators take `progress = { progress }` as a lambda?
Passing a lambda defers reading the `progress` state from the composition phase to the draw/layout phase, preventing the parent composable from recomposing on every animation frame.

### 20. How do `HorizontalPager` and `Carousel` differ?
`HorizontalPager` snaps full pages one at a time using `PagerState` (ideal for onboarding or tabbed swiping). Material 3 `HorizontalMultiBrowseCarousel` / `HorizontalUncontainedCarousel` display multiple peeking items simultaneously with dynamic keyline sizing and `Modifier.maskClip(...)`.
