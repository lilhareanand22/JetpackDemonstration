# LaunchedEffect — Study Guide & Staff Engineer Interview Q&A

---

## Part A: Step-by-Step Architecture & Code Walkthrough

### 1. What is `LaunchedEffect`?

```
┌─────────────────────────────────────────────────────────────┐
│                       LaunchedEffect(key)                   │
├─────────────────────────────────────────────────────────────┤
│ • Enters Composition       ──> Launches a new Coroutine    │
│ • Recomposes (same key)    ──> Keeps running uninterrupted │
│ • Key changes              ──> Cancels old & Launches new  │
│ • Leaves Composition       ──> Cancels the Coroutine       │
└─────────────────────────────────────────────────────────────┘
```

A Composable function should be pure. Calling a suspending function or launching a coroutine directly inside a composable body without a managed effect is a **compile error or critical bug**:
```kotlin
@Composable
fun BadExample() {
    // ❌ WRONG: Calling suspend functions or launch directly in the body
    // will restart on EVERY SINGLE RECOMPOSITION (up to 120 times/sec)!
}
```

Compose provides **`LaunchedEffect(key1, key2, ...)`** to scope a coroutine to the Composable's lifecycle safely.

---

### 2. Section 1 — One-Shot Effects (`key = Unit` or `key = true`)

```kotlin
LaunchedEffect(Unit) {
    onLog("Child entered composition -> STARTED")
    try {
        while (true) {
            delay(1000)
            timerTicks++
        }
    } catch (e: CancellationException) {
        onLog("Child left composition -> CANCELLED")
        throw e
    }
}
```

**Step-by-step mechanism:**
1. **When enters composition**: `LaunchedEffect(Unit)` creates a coroutine in the composition's `CoroutineContext` and starts executing the block.
2. **On subsequent recompositions**: Because the key (`Unit`) never changes, Compose **skips** re-executing this block. The coroutine keeps running smoothly.
3. **When leaves composition**: If the composable is removed (e.g. wrapped in an `if (isVisible)` that becomes `false`), Compose automatically calls `cancel()` on the coroutine job.
4. **CancellationException**: The `delay(1000)` throws a `CancellationException` inside the coroutine, allowing you to run clean-up logic.

---

### 3. Section 2 — Keyed Restart (`LaunchedEffect(category)`)

```kotlin
LaunchedEffect(selectedCategoryId) {
    onLog("Category #$selectedCategoryId -> New fetch started")
    try {
        delay(1500) // Simulated network call
        fetchedData = "Data for Category #$selectedCategoryId"
    } catch (e: CancellationException) {
        onLog("Fetch CANCELLED mid-flight for Category #$selectedCategoryId!")
        throw e
    }
}
```

**Step-by-step mechanism:**
1. When `selectedCategoryId` is `1`, the coroutine begins fetching category 1.
2. Before the 1.5 seconds elapse, the user taps `Category 2`.
3. Because the key changed from `1` to `2`:
   - Compose **immediately cancels** the coroutine for category 1.
   - Compose **immediately launches** a new coroutine for category 2.
4. Any mid-flight network response for category 1 is cleanly aborted, completely avoiding race conditions and stale UI overwrites!

---

### 4. Section 3 — The Debounced Search Pattern

```kotlin
LaunchedEffect(searchQuery) {
    if (searchQuery.isBlank()) return@LaunchedEffect
    
    // 500ms debounce delay
    delay(500)
    
    // Executes ONLY if user paused typing for > 500ms:
    executeNetworkSearch(searchQuery)
}
```

**Step-by-step mechanism:**
- User types `"C"` ➔ Coroutine A starts, enters `delay(500)`.
- User types `"Co"` 100ms later ➔ Coroutine A is cancelled, Coroutine B starts.
- User types `"Com"` 100ms later ➔ Coroutine B is cancelled, Coroutine C starts.
- User stops typing ➔ Coroutine C finishes its 500ms delay and fires the network search!

---

## Part B: Top 10 Staff Engineer Interview Questions & Answers

### Q1. How does `LaunchedEffect` work under the hood in the Compose Runtime?
**A:** When `LaunchedEffect` is invoked, the Compose runtime records an `Effect` node in the slot table:
1. It creates a `CoroutineScope` using `rememberCoroutineScope()` tied to the current `CompositionContext` (inheriting its `CoroutineExceptionHandler` and dispatcher).
2. It stores the provided keys (`key1`, `key2`, etc.) in the slot table.
3. On initial composition, it calls `scope.launch` to execute the block.
4. On recomposition, it compares the previous keys with current keys using structural equality (`equals`). If they match, it does nothing. If any key differs, it cancels the existing `Job` and launches a new coroutine.
5. When the call site leaves composition, the `Job` is automatically cancelled via `Job.cancel()`.

### Q2. What is the difference between `LaunchedEffect(Unit)` and `LaunchedEffect(true)`?
**A:** Practically, **none**. Both `Unit` and `true` are constants whose `equals()` comparison always returns `true`. As a result, the effect will execute exactly once upon entering composition and never restart during recompositions. Idiomatic Kotlin and Google's Compose documentation recommend `LaunchedEffect(Unit)` as the standard convention.

### Q3. Why must you ALWAYS rethrow `CancellationException` in coroutines executed within `LaunchedEffect`?
**A:** In Kotlin Coroutines, cooperative cancellation works by throwing a `CancellationException` when a suspending function checks its job status.
If you catch `Exception` or `Throwable` without rethrowing `CancellationException`:
```kotlin
try {
    delay(1000)
} catch (e: Exception) {
    // ❌ SWALLOWING CANCELLATION!
}
```
The coroutine fails to cancel, remains active in memory, continues to execute subsequent code, and leaks resources. The correct idiom is:
```kotlin
try {
    delay(1000)
} catch (e: CancellationException) {
    throw e // Always propagate cancellation!
} catch (e: Exception) {
    // Handle business errors
}
```

### Q4. Can you call `LaunchedEffect` inside an `onClick` callback or standard button lambda? Why or why not?
**A:** **No.** `LaunchedEffect` is a `@Composable` function. Composable functions can only be invoked from within another Composable function during the Composition phase.
`onClick` is a standard Kotlin non-composable callback `() -> Unit` executed by the event dispatcher during user interaction. To launch a coroutine inside a callback like `onClick`, you must use `rememberCoroutineScope().launch { ... }`.

### Q5. What is the risk of using a mutable object as a key in `LaunchedEffect(myObject)`?
**A:** Compose compares keys using `equals()` (`==`).
- If `myObject` is a mutable class where internal properties mutate without changing its reference, `oldObject == newObject` remains `true`, and `LaunchedEffect` will **fail to restart** when data changes.
- Conversely, if `myObject` is instantiated afresh on every recomposition without `remember`, `old != new` evaluates to true on every single frame, causing the effect to **cancel and restart in an infinite thrashing loop**.
- *Staff Guideline*: Always use immutable primitive values (`String`, `Int`, `UUID`) or stable data classes as keys.

### Q6. Compare `LaunchedEffect` vs. launching coroutines in `ViewModel.viewModelScope`. When should an operation be launched in UI vs ViewModel?
**A:**
- **`viewModelScope` (Preferred for Business Logic)**: Use for data fetching, repository sync, database writes, and business operations that **must survive configuration changes** (screen rotation) or finish even if the user temporarily navigates away.
- **`LaunchedEffect` (UI-Scoped Effects)**: Use strictly for effects that are tied directly to **UI rendering and screen presence**:
  - Animating an `Animatable` or `ScrollState`.
  - Displaying a `Snackbar` on state transition.
  - Triggering a system keyboard open on screen display.
  - If the user rotates or closes the screen, the operation should stop immediately.

### Q7. How do you handle multiple dependent keys in `LaunchedEffect`?
**A:** Compose provides overloaded signatures:
```kotlin
LaunchedEffect(key1, key2) { ... }
LaunchedEffect(key1, key2, key3) { ... }
LaunchedEffect(keys = arrayOf(k1, k2, k3, k4)) { ... }
```
If **any** of the supplied keys change, the ongoing coroutine is cancelled and restarted.

### Q8. What happens if `LaunchedEffect` encounters an unhandled exception?
**A:** Because `LaunchedEffect` is scoped to the `CompositionContext`, an unhandled exception is not swallowed—it bubbles up to the Android CoroutineExceptionHandler and will **crash the application** unless caught inside the block or caught by a custom CoroutineExceptionHandler installed in the parent composition tree.

### Q9. In a code review, an engineer uses `LaunchedEffect(items.size)` to trigger an animation whenever a list updates. What potential bug exists?
**A:** If an item is added and another item is deleted simultaneously, or if items are reordered, `items.size` remains identical.
Because the key (`size`) hasn't changed, `LaunchedEffect` will **not trigger**.
*Fix*: Key on the data content itself (e.g. `LaunchedEffect(items)` where `items` is an immutable list), or maintain a distinct monotonically increasing revision counter.

### Q10. How would you test a Composable containing a `LaunchedEffect` in unit/instrumentation tests?
**A:**
Use `runComposeUiTest` or `createComposeRule()`:
- `ComposeTestRule` automatically synchronizes with the `MainTestClock`.
- Suspending animations or delayed coroutines inside `LaunchedEffect` will advance automatically, or you can manually control timing via `mainClock.advanceTimeBy(millis)` to verify UI states at specific intervals without adding flaky `Thread.sleep()`.
