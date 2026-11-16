package android.ai.jetpackdemonstration.ui.state.model

data class StateTopicItem(
    val id: String,
    val title: String,
    val subtitle: String
)

object StateTopicCatalog {
    val topics: List<StateTopicItem> = listOf(
        StateTopicItem(
            id = "state_hoisting",
            title = "Stateful vs. Stateless & State Hoisting",
            subtitle = "Events flow up, State flows down — the core Compose rule"
        ),
        StateTopicItem(
            id = "remember_saveable",
            title = "remember vs. rememberSaveable",
            subtitle = "Surviving recomposition vs. configuration changes"
        ),
        StateTopicItem(
            id = "state_collections",
            title = "Observable State Collections",
            subtitle = "mutableStateListOf & mutableStateMapOf for reactive lists"
        ),
        StateTopicItem(
            id = "derived_state",
            title = "derivedStateOf",
            subtitle = "Compute derived values without redundant recompositions"
        ),
        StateTopicItem(
            id = "viewmodel_udf",
            title = "ViewModel + StateFlow (UDF)",
            subtitle = "Unidirectional Data Flow with collectAsStateWithLifecycle"
        ),
        StateTopicItem(
            id = "recomposition_scope",
            title = "Recomposition Scoping & Deferred Reads",
            subtitle = "Minimize recomposition via lambdas & layout-phase reads"
        )
    )

    fun findById(id: String): StateTopicItem? = topics.find { it.id == id }
}
