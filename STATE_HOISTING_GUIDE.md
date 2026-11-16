# State Hoisting — Step-by-Step Explanation & Staff Engineer Interview Guide

---

## Part A: Step-by-Step Code Walkthrough

### The Core Rule of Compose State
```
Events flow UP  ↑  (onClick, onValueChange — lambda callbacks)
State  flows DOWN ↓  (value, count — immutable parameters)
```
This is called **Unidirectional Data Flow (UDF)**.

---

### Section 1 — Stateful Counter (Anti-Pattern for Reuse)

```kotlin
@Composable
private fun StatefulCounter() {
    var count by remember { mutableIntStateOf(0) }  // ← state TRAPPED inside
    // ... Button(onClick = { count++ }) ...
}
```

**What happens step by step:**
1. `remember { mutableIntStateOf(0) }` creates a `MutableIntState` and stores it in the **Composition's memory slot** for this composable position.
2. When the button is clicked, `count++` writes a new value to the `MutableIntState`.
3. Compose detects the state change and **schedules a recomposition** for `StatefulCounter`.
4. On recomposition, `remember` returns the *existing* `MutableIntState` (not a new one), so `count` reflects the updated value.

**Why this is a problem:**
- If you place two `StatefulCounter()` calls, they each get their own independent `count` — they **cannot share state**.
- The parent composable cannot read, reset, or control the count.
- Hard to unit test — you must simulate UI clicks to change state.

---

### Section 2 — Stateless Counter (State Hoisted to Parent)

```kotlin
// PARENT — owns the state
@Composable
private fun HoistedCounterSection() {
    var sharedCount by remember { mutableIntStateOf(0) }  // ← single source of truth

    StatelessCounter(
        label = "Child A",
        count = sharedCount,               // State flows DOWN ↓
        onIncrement = { sharedCount++ },   // Event flows UP ↑
        onDecrement = { sharedCount-- }    // Event flows UP ↑
    )
    StatelessCounter(
        label = "Child B",
        count = sharedCount,               // SAME state flows DOWN ↓
        onIncrement = { sharedCount++ },   // SAME event flows UP ↑
        onDecrement = { sharedCount-- }
    )
}

// CHILD — stateless, fully reusable
@Composable
private fun StatelessCounter(
    label: String,
    count: Int,                        // receives state (immutable)
    onIncrement: () -> Unit,           // emits events (callbacks)
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ... displays count, calls onIncrement/onDecrement on button clicks
}
```

**What happens step by step:**
1. `HoistedCounterSection` owns `sharedCount` as the **single source of truth**.
2. It passes `sharedCount` *down* to both Child A and Child B as an immutable `Int` parameter.
3. When Child A's `+` button is tapped, it calls `onIncrement()` which runs `sharedCount++` in the parent.
4. The parent's `sharedCount` state changes → Compose recomposes the parent.
5. The parent passes the *new* `sharedCount` down to **both** Child A and Child B.
6. Both children update simultaneously — they are always in sync.
7. The parent can also reset the count with its own "Reset to 0" button.

**Benefits of hoisting:**
- **Reusable**: `StatelessCounter` works with any `Int` + callbacks.
- **Testable**: Pass `count = 5`, verify it displays "Count: 5", call `onIncrement`, verify callback fired.
- **Shareable**: Multiple siblings read the same single source of truth.
- **Controllable**: Parent can set, validate, or transform the value.

---

### Section 3 — Hoisted TextField (Sibling Synchronization)

```kotlin
@Composable
private fun HoistedTextFieldSection() {
    var name by remember { mutableStateOf("") }  // ← single source of truth

    NameInput(
        value = name,                     // State flows DOWN ↓ to text field
        onValueChange = { name = it }     // Event flows UP ↑ on every keystroke
    )
    NameGreeting(name = name)             // State flows DOWN ↓ to greeting
}
```

**What happens step by step:**
1. User types "J" in the text field inside `NameInput`.
2. `NameInput` calls `onValueChange("J")` → executes `name = "J"` in the parent.
3. Parent recomposes → passes `"J"` down to *both* `NameInput` and `NameGreeting`.
4. `NameInput` displays `"J"` in the text field.
5. `NameGreeting` displays `"Hello, J!"`.
6. User continues typing → `"Jo"` → `"John"` → both siblings update on every keystroke.
7. Parent's "Clear name" button sets `name = ""`, clearing both the text field and the greeting.

---

## Part B: The Hoisting Decision — When to Hoist

| Keep State Local (don't hoist) | Hoist State to Parent |
| :--- | :--- |
| Only this composable needs the state | Multiple composables need to read or write the same state |
| The state is purely presentational (e.g. animation, ripple) | Parent needs to control, validate, or reset the state |
| No need for external testing of the state | State must survive navigation or be shared across screens |
| Example: `AnimatedVisibility` expansion toggle | Example: form fields, search query, selected tab index |

---

## Part C: Top 10 Staff Engineer Interview Questions on State Hoisting

### Q1. What is "State Hoisting" in Jetpack Compose, and what problem does it solve?
**A:** State hoisting is the pattern of moving state ownership from a child composable to its caller (parent), making the child **stateless**. The child receives state as parameters and notifies the parent of events via callbacks. This solves three problems: (1) **reusability** — the same composable works with different state sources; (2) **testability** — you can unit-test by passing known values and asserting callbacks; (3) **single source of truth** — multiple siblings can read the same state without desynchronization.

### Q2. What does "Unidirectional Data Flow" mean in the context of Compose, and how does State Hoisting implement it?
**A:** UDF means data flows in one direction: **state flows down** (parent → child as immutable parameters) and **events flow up** (child → parent as lambda callbacks). State Hoisting implements UDF by ensuring the child never owns mutable state — it receives `value: T` and emits `onValueChange: (T) -> Unit`. The parent is the single source of truth that processes events, updates state, and pushes the new state back down.

### Q3. How does `remember` interact with recomposition, and what would break if you used `mutableIntStateOf(0)` without `remember`?
**A:** `remember` stores an object in the composition's memory slot tied to the composable's call-site position. On recomposition, `remember` returns the *same* stored instance. Without `remember`, every recomposition would create a **new** `mutableIntStateOf(0)`, resetting the counter to 0 on every frame — the state would never increment.

### Q4. In a `TextField(value = text, onValueChange = { text = it })`, what happens if you omit `onValueChange`'s state update?
**A:** The `TextField` is a controlled/stateless component. It renders whatever `value` is passed. If `onValueChange` does not update the backing `text` state, Compose recomposes with the *old* `text` value, effectively **overwriting the user's keystroke**. The text field appears frozen — you type but nothing changes. This is the controlled input pattern from React applied to Compose.

### Q5. As a staff engineer, how do you decide the *right level* to hoist state to? What are the risks of hoisting too high or too low?
**A:** Hoist to the **lowest common ancestor** that needs the state. 
- **Too low**: Siblings that need the same state can't access it, leading to duplicate state and desynchronization.
- **Too high**: Unnecessary recompositions cascade through a large composable tree. For example, hoisting a per-item expansion flag to a screen-level state recomposes the entire list on every expansion toggle.
- **Rule of thumb**: If only one composable reads and writes the state, keep it local. If two siblings share it, hoist to their parent. If unrelated screens need it, promote to a `ViewModel` or a shared state holder.

### Q6. How does Compose know *which* composable to recompose when a `MutableState` changes, and how does state hoisting affect the recomposition scope?
**A:** Compose tracks which composable functions **read** a `State` object during composition. When that `State` is written, only the composables that read it are scheduled for recomposition (this is called **fine-grained reactivity**). With state hoisting, the `MutableState` is read in the parent, so the parent's scope recomposes and re-invokes the children with new parameters. If the children's parameters haven't changed (checked via `equals`), Compose can **skip** their recomposition — this is why using `data class` or primitive parameters helps.

### Q7. What is the relationship between `MutableState`, `MutableIntState`, and the `by` delegate syntax? Why does Compose offer `mutableIntStateOf` separately?
**A:** `mutableStateOf<Int>(0)` wraps the `Int` in a `MutableState<Int>` object, which boxes the primitive on every read/write. `mutableIntStateOf(0)` stores the value as an unboxed `int` field internally, avoiding auto-boxing overhead. The `by` keyword uses Kotlin property delegation: `var count by mutableIntStateOf(0)` lets you write `count++` instead of `state.intValue++`, making the code read like a normal variable while Compose still tracks reads and writes under the hood.

### Q8. How would you design a reusable form component with multiple fields using state hoisting? Describe the state holder pattern.
**A:** Create a **state holder class** (plain class, not a ViewModel) that encapsulates all form fields and validation:
```kotlin
class FormState(initialName: String = "", initialEmail: String = "") {
    var name by mutableStateOf(initialName)
    var email by mutableStateOf(initialEmail)
    val isValid: Boolean get() = name.isNotBlank() && email.contains("@")
}

@Composable
fun rememberFormState() = remember { FormState() }
```
The parent creates `rememberFormState()` and passes it (or its individual fields + callbacks) to child composables. This keeps state collocated, testable (instantiate `FormState` in tests, set fields, assert `isValid`), and hoisted to the right level without polluting a ViewModel with UI-only concerns.

### Q9. When should state live in a `ViewModel` vs. inside `remember`/`rememberSaveable`? What is the lifecycle difference?
**A:** 
- **`remember`**: Survives recomposition, lost on configuration change (rotation) and process death.
- **`rememberSaveable`**: Survives recomposition AND configuration change (uses `SavedStateHandle` / `Bundle`), lost on process death (unless combined with `SavedStateHandle`).
- **`ViewModel`**: Survives recomposition AND configuration change, scoped to `ViewModelStoreOwner` (Activity, Fragment, NavBackStackEntry). Lost on process death unless using `SavedStateHandle`.

Use `remember` for transient UI state (animation, hover). Use `rememberSaveable` for user-entered data that must survive rotation (form text). Use `ViewModel` for business/domain state, data from repositories, and state that must survive navigation within a `NavHost`.

### Q10. In a code review, a junior developer has hoisted `LazyListState` to a ViewModel. What feedback would you give?
**A:** `LazyListState` is a **UI-layer concern** — it tracks scroll position and visible items for rendering. Hoisting it to a ViewModel violates separation of concerns because ViewModels should not hold Compose-specific types (they shouldn't depend on `compose-foundation`). Instead, keep `LazyListState` in the composable with `rememberLazyListState()` (which already uses `rememberSaveable` internally to restore scroll position across config changes). If the ViewModel needs to *react* to scroll position (e.g., to load more data), expose a callback like `onScrolledToBottom: () -> Unit` that flows the event up — not the scroll state object itself.
