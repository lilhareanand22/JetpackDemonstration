# Recomposition Scoping & Deferred State Reads — Study Guide & Staff Engineer Interview Q&A

---

## Part A: Step-by-Step Architecture & Code Walkthrough

### 1. The Three Phases of a Jetpack Compose Frame

Every frame in Jetpack Compose executes through up to **three distinct phases**:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. COMPOSITION PHASE                                         │
│ What to show: Executes @Composable functions and builds the  │
│ UI tree (LayoutNode tree).                                  │
├─────────────────────────────────────────────────────────────┤
│ 2. LAYOUT PHASE                                              │
│ Where to place it: Measures children and decides x, y        │
│ coordinates (MeasureScope & Placeable.placeRelative).        │
├─────────────────────────────────────────────────────────────┤
│ 3. DRAW PHASE                                                │
│ How to render it: Emits draw calls to the Skia/canvas        │
│ pipeline (DrawScope, Modifier.drawBehind).                  │
└─────────────────────────────────────────────────────────────┘
```

**The Core Performance Principle:**
> **"Defer state reads to the latest possible phase."**
> If state only affects placement or drawing, reading it during Composition forces the entire composable tree to re-execute every frame. Reading it in Layout or Draw skips Composition completely!

---

### 2. Section 1 — Localized Recomposition Scoping

```kotlin
// Parent Composable
var parentCounter by remember { mutableIntStateOf(0) }
var isolatedChildCounter by remember { mutableIntStateOf(0) }
```

**Step-by-step mechanism:**
1. Compose automatically creates **RecomposeScopes** around restartable composables (non-inline functions with `@Composable`).
2. When `parentCounter` changes, the parent composable reads it. The parent scope is invalidated and recomposes, re-evaluating its children.
3. When `isolatedChildCounter` changes, **only Child B reads it**. The parent has zero read dependencies on that state.
4. Therefore, **the parent completely skips recomposition!** Only Child B re-executes.
5. In the demo, you can verify this live: tapping `+ Child Counter` increments Child B's counter without touching the parent's recomposition count.

---

### 3. Section 2 — Deferred Reads via Lambda (`Modifier.offset`)

Consider an animation or slider driving an offset:

#### ❌ Non-Deferred (Direct State Read in Composition):
```kotlin
Box(
    modifier = Modifier.offset(x = offsetX.dp) // ← Reads offsetX during Composition!
)
```
- Every micro-change in `offsetX` triggers full recomposition of `NonDeferredBox`.
- If dragged across 150 pixels, the function recomposes **150+ times**, allocating modifier chains and putting heavy pressure on the Compose compiler.

#### ✅ Deferred (Lambda Read in Layout Phase):
```kotlin
Box(
    modifier = Modifier.offset { IntOffset(x = offsetX.roundToInt(), y = 0) }
)
```
- Notice that `offset { ... }` takes a **lambda** `Density.() -> IntOffset`.
- During Composition, Compose creates the modifier and stores the lambda. **`offsetX` is NOT read during composition!**
- The lambda is only invoked later during the **Layout phase** (`Placeable.placeRelative`).
- Result: **0 recompositions!** The slider moves smoothly at 120fps with zero composable re-invocations.

---

### 4. Section 3 — Draw-Phase State Reads (`Modifier.drawBehind`)

```kotlin
Box(
    modifier = Modifier.drawBehind {
        // Read colorHue strictly in DrawScope
        drawRect(color = Color.hsl(hue = colorHue, saturation = 0.7f, lightness = 0.5f))
    }
)
```
- If a property only affects colors, alphas, or canvas paths, read it inside `drawBehind` or `Modifier.graphicsLayer { alpha = ... }`.
- **Both the Composition and Layout phases are skipped entirely.** The Skia canvas simply repaints with the new color without recalculating any view boundaries or re-executing Kotlin composable code.

---

## Part B: Top 10 Staff Engineer Interview Questions & Answers

### Q1. What are the three phases of Compose, and how does Compose track which phase a state is read in?
**A:** The three phases are:
1. **Composition**: Runs composables to construct or update the layout node tree.
2. **Layout**: Measures and places nodes (`MeasurePolicy`).
3. **Draw**: Renders nodes to the Canvas (`DrawScope`).

Under the hood, the Compose Snapshot system tracks state reads using `Snapshot.observe(readObserver) { block() }`. When entering Composition, Compose sets a reader that records state dependencies on the active `RecomposeScope`. When entering the Layout phase, a different reader observes layout measurements. When entering Draw, another reader records draw dependencies. If a state is only read inside `drawBehind { ... }`, Compose only flags the node's draw layer as dirty, bypassing the first two phases.

### Q2. What is a "Restartable" vs. "Non-Restartable" Composable, and how does the Compose Compiler determine them?
**A:**
- **Restartable Composable**: The Compose compiler wraps the function in a `RecomposeScopeImpl`. When state read inside it changes, Compose can re-invoke this function without restarting its parent.
- **Non-Restartable Composable**: The function cannot act as a restart boundary. Examples include inline composables (like `Box`, `Row`, `Column`), composables returning a value, or functions annotated with `@NonRestartableComposable`.
- *Staff insight*: Because `Row` and `Column` are inline, state read directly inside a `Row` or `Column` body without a wrapper composable actually invalidates the **surrounding parent function**, not just the row!

### Q3. Why does passing a lambda (e.g., `() -> Float`) instead of a raw value (`Float`) prevent parent recomposition?
**A:**
- If you pass `value: Float`, the parent must read the state to evaluate the parameter before invoking the child: `Child(counter)`. This registers a read dependency on the parent's scope, causing the parent to recompose whenever `counter` changes.
- If you pass `value: () -> Float`, the parent only creates and passes a lambda reference: `Child({ counter })`. The parent **never reads the state property itself**. The read only happens when the child (or modifier) invokes the lambda inside its own isolated scope or during the layout/draw phase.

### Q4. Compare `Modifier.offset(x, y)` and `Modifier.offset { IntOffset(x, y) }`. Under what circumstances should you use each?
**A:**
- Use `Modifier.offset(x, y)` only when `x` and `y` are **static constants** or change very rarely.
- Use `Modifier.offset { IntOffset(x, y) }` whenever the offset is **dynamic, animated, or driven by gesture/scroll** (e.g. swipe-to-dismiss, scroll parallax, dragging). The lambda version defers execution to the Layout phase, preventing 60fps/120fps recomposition thrashing.

### Q5. What is `Modifier.graphicsLayer { ... }`, and why is it preferred for alpha and scale animations over `Modifier.alpha()`?
**A:**
- `Modifier.alpha(alpha)` reads alpha during Composition and applies it via a modifier node update.
- `Modifier.graphicsLayer { this.alpha = alphaProvider() }` executes during the **Draw phase**. It tells the Android render thread / hardware layer to adjust layer opacity on the GPU directly without measuring or laying out the node again.
- In animations, using the lambda version of `graphicsLayer` guarantees zero recomposition and zero layout passes, running buttery smooth on the RenderThread.

### Q6. How does Compose determine whether a Composable can be "Skipped" during recomposition? What are Stable and Immutable types?
**A:** Compose can skip a composable if all its parameters are **Stable** and their values are equal (`equals()`) to their previous values.
- **`@Immutable`**: Promises that all public properties are immutable (`val`) and will never change after construction.
- **`@Stable`**: Promises that if public properties change, Compose will be notified via Snapshot state, and `equals` is consistent.
- **Unstable Types**: Standard interfaces (like `List<T>`), classes from external modules without the Compose compiler, or classes with `var` properties are considered unstable by default. A composable accepting an unstable type is marked as **non-skippable** and will recompose every time its parent recomposes, even if parameters haven't changed!

### Q7. How does the Compose Compiler 2.0+ (and Strong Skipping Mode) change stability rules?
**A:** In Kotlin 2.0+ with **Strong Skipping Mode** (now enabled by default in modern Compose):
- Composables with unstable parameters can now be skipped if the parameters evaluate to the same instance by referential equality (`===`) or structural equality.
- Lambdas with unstable captures are automatically remembered using `remember(capturedValues)`.
- *Staff insight*: While Strong Skipping reduces boilerplate `@Immutable` annotations, developers must still ensure `equals` is fast and avoid passing freshly allocated objects on every frame.

### Q8. What is the difference between `derivedStateOf` and reading state inside a deferred phase lambda?
**A:**
- `derivedStateOf` is used when a state updates frequently (e.g., scroll offset in pixels), but your UI only cares when a derived condition transitions (e.g., `scrollOffset > 100`). It clamps updates to only fire recomposition when the boolean result flips.
- **Deferred phase lambdas** (`offset { }`, `drawBehind { }`) don't prevent state reads; they **move the state read out of Composition entirely** into Layout or Draw.
- *Rule*: If the value determines *what composables to add or remove* (`if (showButton)`), use `derivedStateOf`. If the value only determines *where to position or how to draw* a composable, use a deferred lambda.

### Q9. How do you profile and detect unnecessary recompositions in a production app?
**A:**
1. **Layout Inspector in Android Studio**: Turn on **Recomposition Counts** and **Recomposition Highlights** to see which composables re-render in real time.
2. **Compose Compiler Metrics**: Enable compiler flags (`reportsDestination` and `metricsDestination`) to generate `.txt` reports detailing which composables are restartable/skippable and which classes are unstable.
3. **Android Studio Profiler (Systrace / Perfetto Traces)**: Look for recurring `Recompose` slices in the trace during scrolling or animation. If `Recompose` slices match the frame rate, state reads are leaking into the Composition phase.

### Q10. In a code review, an engineer wrote:
```kotlin
@Composable
fun PulsingBox(scale: Float) {
    Box(modifier = Modifier.size(100.dp * scale))
}
```
### What is the performance defect during an ongoing pulse animation, and how do you refactor it?
**A:**
- **Defect**: Mutating `Modifier.size(100.dp * scale)` on every animation frame triggers **Recomposition AND Layout (remeasurement)** of the Box and potentially all its parent containers on every single frame, causing frame drops and UI jank.
- **Refactoring**:
  ```kotlin
  @Composable
  fun PulsingBox(scaleProvider: () -> Float) {
      Box(
          modifier = Modifier
              .size(100.dp)
              .graphicsLayer {
                  scaleX = scaleProvider()
                  scaleY = scaleProvider()
              }
      )
  }
  ```
  This measures the Box once at 100.dp during the initial layout. During the pulse animation, the scale is applied strictly as a GPU render transformation in the Draw phase. Recomposition count = 0, Layout count = 0!
