# remember vs. rememberSaveable — Study Guide & Staff Engineer Interview Q&A

---

## Part A: Step-by-Step Architecture & Code Walkthrough

### 1. The Core Lifecycle Difference

```
┌─────────────────────────────────────────────────────────────┐
│ Event                       │ remember  │ rememberSaveable  │
├─────────────────────────────┼───────────┼───────────────────┤
│ Recomposition               │ PRESERVED │ PRESERVED         │
│ Configuration Change        │ LOST (0)  │ PRESERVED         │
│ (Screen rotation, dark mode)│           │ (via Bundle)      │
│ System-initiated Kill       │ LOST (0)  │ PRESERVED         │
│ (Process Death / Low RAM)   │           │ (via SavedState)  │
│ App Back / Explicit Finish  │ CLEARED   │ CLEARED           │
└─────────────────────────────────────────────────────────────┘
```

---

### 2. Section 1 — Rotation Survival Test (Counter)

```kotlin
// ❌ remember: stored in memory tied to Composition's slot table
var rememberCount by remember { mutableIntStateOf(0) }

// ✅ rememberSaveable: stored in Composition slot table AND registered with SavedStateRegistry
var saveableCount by rememberSaveable { mutableIntStateOf(0) }
```

**Step-by-step mechanism:**
1. **Initial composition**: Both slots initialize to `0`.
2. **User increments both to 5**:
   - `remember` holds `5` in memory.
   - `rememberSaveable` holds `5` in memory and registers a provider with `SaveableStateRegistry`.
3. **Screen rotates (Configuration Change)**:
   - The Activity / Composition is destroyed and recreated.
   - **`remember`**: The slot table is completely discarded. When `remember { mutableIntStateOf(0) }` executes again in the new Composition, it evaluates the initial lambda and resets to `0`.
   - **`rememberSaveable`**: Before destruction, the Android system triggers `onSaveInstanceState(outState: Bundle)`. `rememberSaveable` saves `5` into the Bundle under a generated or explicit key. On recreation, it restores the value from the `SavedStateRegistry` instead of calling the init lambda.

---

### 3. Section 2 — Form Field Persistence

```kotlin
var transientText by remember { mutableStateOf("") }
var persistentText by rememberSaveable { mutableStateOf("") }
```

- When filling out a long form, losing entered text due to screen rotation or system theme change creates massive user frustration.
- `rememberSaveable` stores primitive types (`String`, `Int`, `Boolean`, `Float`, etc.) directly into the `Bundle` without extra boilerplate because `Bundle` natively supports them.

---

### 4. Section 3 — Custom Object Persistence (`Saver`, `listSaver`, `mapSaver`)

Android `Bundle` cannot store arbitrary Kotlin `data class` instances without instructions. If you call:
```kotlin
// ❌ Runtime crash if UserProfile is not Parcelable or has no Saver:
var profile by rememberSaveable { mutableStateOf(UserProfile("AndroidDev", 1)) }
```

To solve this without adding `@Parcelize` or Android-specific dependencies, Compose provides the **`Saver`** contract:

#### Option 1: `listSaver`
```kotlin
val UserProfileListSaver = listSaver<UserProfile, Any>(
    save = { listOf(it.username, it.level) },
    restore = { UserProfile(username = it[0] as String, level = it[1] as Int) }
)
```
- Converts `UserProfile` into a `List<Any>` of Bundle-compatible primitives on save.
- Re-constructs `UserProfile` from list indices on restore.

#### Option 2: `mapSaver`
```kotlin
val UserProfileMapSaver = mapSaver<UserProfile, Any>(
    save = { mapOf("username" to it.username, "level" to it.level) },
    restore = {
        UserProfile(
            username = it["username"] as String,
            level = it["level"] as Int
        )
    }
)
```
- Safer for classes with many fields, avoiding index mistakes.

---

## Part B: Top 10 Staff Engineer Interview Questions & Answers

### Q1. How does `rememberSaveable` work under the hood in Compose runtime?
**A:** `rememberSaveable` acts as a bridge between the Compose **Slot Table** and Android's **`SaveableStateRegistry`** (which hooks into `SavedStateRegistryOwner` / `onSaveInstanceState`).
1. When composed, it registers a `SaverProvider` with the ambient `LocalSaveableStateRegistry`.
2. Before the Activity/Fragment is torn down during configuration changes or process death, the registry queries all registered providers and packs their outputs into the system `Bundle`.
3. When the composable is reinvoked in the new Activity/Window, `rememberSaveable` checks the registry for a previously saved value matching its derived key. If found, it calls `Saver.restore()`; otherwise, it executes the fallback initialization lambda.

### Q2. How is the save key determined for `rememberSaveable` if no explicit `key` parameter is passed?
**A:** Compose automatically derives an integer key based on the **call site source position** (compilation slot index) and the hierarchical path in the composition tree. 
*Caution for Staff Engineers*: If you use `rememberSaveable` inside dynamic items (such as a loop, `if/else`, or `LazyColumn` without a key), positional shift can cause the wrong restored state to be mapped to an item. In dynamic contexts, always pass an explicit `key = "user_${item.id}"` to ensure deterministic state restoration.

### Q3. What are the dangers of storing large collections or bitmaps in `rememberSaveable`?
**A:** `rememberSaveable` serializes data into the Android system **TransactionBuffer / `Bundle`**.
- The Android Binder transaction buffer has a hard limit across all ongoing transactions for the entire process (typically **1 MB**).
- Exceeding this limit throws a `TransactionTooLargeException`, crashing the app.
- *Best practice*: Never store images, large datasets, or pagination caches in `rememberSaveable`. Only store small metadata (e.g. selected IDs, scroll offset, draft strings). Large data belongs in repository/disk cache (Room, DataStore) or in-memory ViewModel cache.

### Q4. Compare `remember`, `rememberSaveable`, and `SavedStateHandle` in a ViewModel. When should each be used?
**A:**
- **`remember`**: Component-internal UI state. Survives recomposition only. Lost on rotation and process death. *Use case: UI animations, focus state, ripple effects, tooltip visibility.*
- **`rememberSaveable`**: UI-scoped transient state that must survive config changes and process death, but doesn't belong in business logic. *Use case: active scroll position, current tab index, unsaved form text.*
- **`SavedStateHandle` (in ViewModel)**: Business/navigation state tied to the screen lifecycle that must survive process death and navigate-back scenarios. *Use case: search query triggering repository fetches, navigation arguments, active filter IDs.*

### Q5. What is the difference between `@Parcelize` and a custom Compose `Saver`? When would you prefer `Saver` over `@Parcelize`?
**A:**
- `@Parcelize` requires Android framework dependencies (`android.os.Parcelable`), tying the data class to the Android SDK.
- `Saver<Original, Saveable>` is part of Compose Runtime (`androidx.compose.runtime.saveable`). It can map pure Kotlin models or Kotlin Multiplatform (KMP) data classes into primitives (`listSaver`, `mapSaver`) without bringing in Android OS dependencies.
- In multiplatform projects (Compose Multiplatform for Desktop/iOS/Web), `Saver` is portable, whereas `Parcelize` is Android-specific.

### Q6. How does `rememberSaveable` behave inside a `LazyColumn` or `LazyRow`?
**A:** Items in a `LazyColumn` leave the composition when scrolled off-screen and re-enter when scrolled back in.
- Plain `remember` loses its state the moment the item leaves the viewport and is recycled/disposed.
- `rememberSaveable` combined with `key` on `items(...)` retains item-level state (e.g. a checkbox or text field inside an individual row) even when scrolled far off-screen.
- The `LazyList` uses `SaveableStateHolder` internally to save and restore each child's sub-composition.

### Q7. What happens if a class used in `rememberSaveable` is modified between app releases and the user restores from a saved state?
**A:** If fields are added, renamed, or deleted in a custom class saved via `listSaver` or `Parcelable`, attempting to deserialize the old bundle format can lead to a `ClassCastException`, `IndexOutOfBoundsException`, or silent field mismatch on process restore.
*Staff Engineer Solution*: Use `mapSaver` with default fallback values (`map["version"]`, `map["username"] ?: ""`) or version-tag saved payloads to gracefully handle schema migrations during process recreation.

### Q8. How do you unit-test or verify that a custom `Saver` correctly restores its state?
**A:** You can write a pure JVM unit test without launching an emulator by testing the `Saver` contract directly:
```kotlin
@Test
fun testUserProfileSaver() {
    val original = UserProfile(username = "Alice", level = 42)
    val saver = UserProfileListSaver
    
    // Simulate save
    val saved = saver.save(SaverScope { true }, original)
    
    // Simulate restore
    val restored = saver.restore(saved!!)
    
    assertEquals(original, restored)
}
```

### Q9. Can you use `rememberSaveable` with `mutableStateListOf` or `mutableStateMapOf` directly?
**A:** Not directly with standard syntax, because `mutableStateListOf()` returns a `SnapshotStateList` which does not implement `Parcelable`.
To persist a list across rotation using `rememberSaveable`:
```kotlin
val list = rememberSaveable(
    saver = listSaver(
        save = { it.toList() },
        restore = { it.toMutableStateList() }
    )
) {
    mutableStateListOf<String>()
}
```
Or store an immutable `List<T>` inside a `mutableStateOf<List<T>>(emptyList())` using `rememberSaveable`.

### Q10. In a code review, an engineer replaced all `remember` calls with `rememberSaveable` throughout a design system library. What do you say?
**A:** **Reject the PR.**
1. **Performance Overhead**: `rememberSaveable` registers providers, allocates Bundle keys, and performs serialization/deserialization cycles on state changes and lifecycle events.
2. **TransactionBuffer Pollution**: Micro-components (button states, hover flags, animations) pollute the process `Bundle`, risking `TransactionTooLargeException`.
3. **Improper Scope**: Transient UI states (e.g., whether an icon is pressed, whether an accordion animation is playing) *should* reset on configuration changes. Only state that represents user intent or user-created data warrants persistence.
