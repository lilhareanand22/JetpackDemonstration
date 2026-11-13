# ViewModel + StateFlow (Unidirectional Data Flow) — Study Guide & Staff Engineer Interview Q&A

---

## Part A: Step-by-Step Architecture & Code Walkthrough

### 1. The Core Architectural Pattern (MVI / UDF)

```
        ┌────────────────────────────────────────────────────────┐
        │                      VIEWMODEL                         │
        │                                                        │
        │   ┌────────────────────────────────────────────────┐   │
        │   │               Business Logic                   │   │
        │   │  (Repository calls, input validation, network) │   │
        │   └────────────────────────────────────────────────┘   │
        │              │                                ▲        │
        │              ▼                                │        │
        │     _uiState.update { ... }                   │        │
        │              │                       onAction(Action)  │
        │              ▼                                │        │
        │      StateFlow<UiState>                       │        │
        └──────────────┬────────────────────────────────┴────────┘
                       │ (State flows DOWN ↓)           │ (Events flow UP ↑)
                       ▼                                │
        ┌────────────────────────────────────────────────────────┐
        │                   COMPOSE UI LAYER                     │
        │                                                        │
        │   val uiState by                                       │
        │     viewModel.uiState.collectAsStateWithLifecycle()    │
        │                                                        │
        │   when (val state = uiState) {                         │
        │       is Loading -> ShowSpinner()                      │
        │       is Success -> ShowList(state.articles)           │
        │       is Error   -> ShowError(state.errorMessage)      │
        │   }                                                    │
        └────────────────────────────────────────────────────────┘
```

---

### 2. Step 1: Model UI State as a Sealed Interface

```kotlin
sealed interface ArticlesUiState {
    data object Loading : ArticlesUiState
    data class Success(
        val articles: List<String>,
        val isRefreshing: Boolean = false
    ) : ArticlesUiState
    data class Error(val errorMessage: String) : ArticlesUiState
}
```
**Why sealed interface?**
- **Exhaustive pattern matching**: When writing `when (uiState)`, the Kotlin compiler forces you to handle all possible states (`Loading`, `Success`, `Error`). No forgotten edge cases!
- **Impossible states are unrepresentable**: With separate flags (`isLoading: Boolean`, `error: String?`, `data: List?`), you can end up with `isLoading = true` AND `error != null` at the same time. A sealed hierarchy makes that invalid by design.

---

### 3. Step 2: Single Entry Point for Events (`onAction`)

```kotlin
sealed interface ArticleUserAction {
    data object Refresh : ArticleUserAction
    data class DeleteArticle(val title: String) : ArticleUserAction
    data object TriggerSimulatedError : ArticleUserAction
}
```
- Instead of exposing 10 different public ViewModel methods (`refresh()`, `delete()`, `retry()`), expose a **single entry point**:
  ```kotlin
  fun onAction(action: ArticleUserAction)
  ```
- This enforces strict Unidirectional Data Flow, makes analytics logging effortless, and simplifies UI testing.

---

### 4. Step 3: `collectAsStateWithLifecycle()` vs `collectAsState()`

```kotlin
@Composable
fun ViewModelUdfDemo(viewModel: ArticleViewModel = viewModel()) {
    // ✅ collectAsStateWithLifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArticleScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}
```

| Method | When App goes to Background (`onStop`) | Risk / Problem |
| :--- | :--- | :--- |
| `collectAsState()` | **Keeps collecting Flow events actively** | Wastes CPU/battery, can crash if updating UI off-screen |
| `collectAsStateWithLifecycle()` | **Cancels collection below `STARTED`** | Zero CPU wasted, resumes automatically when app returns to foreground |

---

### 5. Step 4: Stateful Host vs. Stateless Content

- **`ViewModelUdfDemo` (Stateful Host)**: Obtains the ViewModel instance and collects `StateFlow`.
- **`ArticleScreenContent` (Stateless Content)**: Accepts raw `ArticlesUiState` and `onAction: (ArticleUserAction) -> Unit`.
  - **Zero ViewModel dependency!**
  - Instant `@Preview` support by passing mock data: `ArticlesUiState.Success(listOf("Article 1"))`.
  - Simple Compose UI testing without mocking ViewModel classes.

---

## Part B: Top 10 Staff Engineer Interview Questions & Answers

### Q1. Why is `collectAsStateWithLifecycle()` recommended over `collectAsState()` in modern Android Compose development?
**A:** `collectAsState()` is lifecycle-agnostic: it starts collection when entering composition and only cancels when leaving composition. If an Activity or Fragment moves to the background (lifecycle state `STOPPED`), `collectAsState()` continues collecting from the underlying `StateFlow`.
`collectAsStateWithLifecycle()` uses `repeatOnLifecycle(Lifecycle.State.STARTED)` under the hood. When the app is in the background or the screen is not visible to the user, it **cancels the coroutine collecting the Flow**, preventing unnecessary background resource usage (network calls, database queries, sensor ticks) and avoiding crashes from processing events when the UI is invisible.

### Q2. How should one-off UI events (e.g. showing a Snackbar, navigating to another screen) be modeled in modern UDF? Is `SharedFlow` with replay=0 acceptable?
**A:** Historically, developers used `Channel` or `SharedFlow(replay = 0)` for "single-live events". However, Google's official Android Architecture guidelines advise that **UI events should be modeled directly in UI state** or handled as direct callbacks:
- **Navigation**: The UI passes an event up to the caller lambda (`onNavigateToDetail(id)`), which executes navigation directly.
- **Transitory messages (Snackbar)**: Include the message in the UI state:
  ```kotlin
  data class ScreenUiState(val userMessage: String? = null, ...)
  ```
  Once the UI displays the Snackbar, it dispatches an action `onAction(DismissUserMessage)` to clear the field.
- **Why?** `SharedFlow(replay = 0)` can lose events if emitted before the UI starts collecting (e.g., during screen rotation when the collector is temporarily cancelled). Modeling in state guarantees the message survives until explicitly consumed.

### Q3. When should a screen have a Single State class vs. Multiple StateFlow streams?
**A:**
- **Single State (`StateFlow<ScreenUiState>`)**: Strongly preferred for related screen-level states. Guarantees atomic state transitions (e.g. loading switches to success in a single frame without intermediate tearing/glitches). Prevents race conditions between interdependent properties.
- **Multiple StateFlows**: Useful when states have completely independent update cadences and consumers (e.g., a high-frequency real-time timer stream vs. a static user profile stream), avoiding recomposition of the profile UI whenever the timer ticks.

### Q4. What is the difference between `_uiState.value = ...` and `_uiState.update { ... }` in Kotlin coroutines?
**A:** `_uiState.update { current -> current.copy(...) }` is **thread-safe and atomic**:
- It uses a lock-free Compare-And-Set (`CAS`) loop internally.
- If two coroutines simultaneously execute `_uiState.update`, neither update is lost.
- Direct assignment `_uiState.value = _uiState.value.copy(...)` is subject to race conditions: if Coroutine A and Coroutine B read `_uiState.value` at the same time, Coroutine B's write will overwrite Coroutine A's changes without incorporating them.

### Q5. In a multi-module architecture, should UI modules depend on ViewModels directly? How do you maintain clean separation?
**A:** The **Stateless Content Pattern** should be strictly enforced:
1. Pure UI components (Design System, reusable screens) live in feature/UI modules and accept **only plain data classes and lambda callbacks**. They never depend on Android `ViewModel` or `androidx.lifecycle.viewmodel`.
2. A thin "Screen Route" or "Host" composable binds the `ViewModel` to the stateless content.
3. This allows the UI to be previewed in Android Studio, tested with Roborazzi/Paparazzi screenshot tests, and reused across platforms (Compose Multiplatform) where `androidx.lifecycle.ViewModel` does not exist.

### Q6. How do you handle initial data loading in a ViewModel? Is `init { loadData() }` an anti-pattern?
**A:** `init { loadData() }` is simple but has drawbacks in large-scale architectures:
- **Drawback**: It starts fetching data the moment the ViewModel is instantiated, even if the user never displays the screen or if testing the ViewModel in isolation.
- **Staff-preferred pattern**: Use lazy cold flow operators like `stateIn` with `SharingStarted.WhileSubscribed(5000)`:
  ```kotlin
  val uiState: StateFlow<UiState> = repository.getArticlesStream()
      .map { UiState.Success(it) }
      .stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5_000),
          initialValue = UiState.Loading
      )
  ```
  Data is only requested when there is an active subscriber (the UI is on-screen), and the 5-second timeout bridges screen rotation without restarting the upstream fetch!

### Q7. What is the purpose of the 5000ms delay in `SharingStarted.WhileSubscribed(5000)`?
**A:** When an Activity rotates or undergoes a configuration change, the old Activity stops and destroys before the new Activity starts and resubscribes to the ViewModel's `StateFlow`.
- Without a buffer, the subscriber count drops to 0 during the few milliseconds of rotation, immediately cancelling the upstream flow and restarting the network fetch upon recreation.
- The 5,000ms grace period keeps the upstream flow alive during rotation, while still cancelling it if the user actually exits the screen (presses Back or Home for >5 seconds).

### Q8. How do you unit-test a ViewModel that uses `viewModelScope` and `StateFlow`?
**A:**
1. Set the Main dispatcher using `StandardTestDispatcher` and `Dispatchers.setMain(testDispatcher)`.
2. Use **Turbine** (`app.cash.turbine`) to test `StateFlow` emissions cleanly:
   ```kotlin
   @Test
   fun `when refresh action dispatched, then emits Loading and Success`() = runTest {
       val viewModel = ArticleViewModel(repository)
       viewModel.uiState.test {
           assertEquals(ArticlesUiState.Loading, awaitItem())
           viewModel.onAction(ArticleUserAction.Refresh)
           assertEquals(ArticlesUiState.Success(...), awaitItem())
       }
   }
   ```

### Q9. How do you avoid state explosion when a screen has dozens of complex nested tabs and dialogs?
**A:** Use the **Hierarchical State Holder** pattern:
- The screen ViewModel coordinates high-level business state (authentication, repository entities).
- UI-only complexity (e.g., active tab, search filter chips, open dialog visibility) is encapsulated in a dedicated UI State Holder class:
  ```kotlin
  class FilterSheetState(val scrollState: ScrollState) {
      var isVisible by mutableStateOf(false)
  }
  @Composable
  fun rememberFilterSheetState() = remember { FilterSheetState(...) }
  ```
- This keeps the ViewModel focused on business logic and prevents the screen state data class from growing into an unmaintainable 50-field monster.

### Q10. In a code review, an engineer passed a `ViewModel` directly into 4 levels of nested child composables. What feedback do you provide?
**A:** **Reject with architecture feedback:**
1. **Tight Coupling**: Deep child composables become impossible to preview with `@Preview` or reuse in different contexts because they require an instantiated `ViewModel`.
2. **Hidden Dependencies**: It obscures what data a child actually requires.
3. **Refactoring Rule**: Only the root screen composable should know about the `ViewModel`. It extracts state and passes explicit slices (`articles`, `isLoading`) down to children, alongside specific event lambdas (`onDelete = { viewModel.onAction(...) }`).
