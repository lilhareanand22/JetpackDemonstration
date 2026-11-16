package android.ai.jetpackdemonstration.ui.sideeffects.model

data class SideEffectTopicItem(
    val id: String,
    val title: String,
    val subtitle: String
)

object SideEffectTopicCatalog {
    val topics: List<SideEffectTopicItem> = listOf(
        SideEffectTopicItem(
            id = "launched_effect",
            title = "LaunchedEffect",
            subtitle = "Safely run suspending coroutines scoped to composition & keys"
        ),
        SideEffectTopicItem(
            id = "remember_coroutine_scope",
            title = "rememberCoroutineScope",
            subtitle = "Launch coroutines outside composition (onClick, gestures)"
        ),
        SideEffectTopicItem(
            id = "remember_updated_state",
            title = "rememberUpdatedState",
            subtitle = "Capture latest state in long-running effects without restarting them"
        ),
        SideEffectTopicItem(
            id = "disposable_effect",
            title = "DisposableEffect",
            subtitle = "Manage listeners with onDispose cleanup contract"
        ),
        SideEffectTopicItem(
            id = "side_effect",
            title = "SideEffect",
            subtitle = "Publish state to non-Compose objects on every successful recomposition"
        ),
        SideEffectTopicItem(
            id = "produce_state",
            title = "produceState",
            subtitle = "Convert async data streams directly into Compose State"
        ),
        SideEffectTopicItem(
            id = "snapshot_flow",
            title = "snapshotFlow",
            subtitle = "Bridge Compose State to Kotlin Flow with reactive operators"
        ),
        SideEffectTopicItem(
            id = "derived_state",
            title = "derivedStateOf",
            subtitle = "Compute derived State to filter out high-frequency recompositions"
        )
    )

    fun findById(id: String): SideEffectTopicItem? = topics.find { it.id == id }
}
