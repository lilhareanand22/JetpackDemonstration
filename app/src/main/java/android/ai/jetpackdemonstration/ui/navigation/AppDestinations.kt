package android.ai.jetpackdemonstration.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data object ComponentsRoute

@Serializable
data object StateRoute

@Serializable
data object SideEffectsRoute

@Serializable
data class ComponentDetailRoute(val componentId: String)

@Serializable
data class StateDetailRoute(val topicId: String)

enum class TopLevelDestination(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Components(
        route = ComponentsRoute,
        label = "Components",
        selectedIcon = Icons.Rounded.Widgets,
        unselectedIcon = Icons.Outlined.Widgets
    ),
    State(
        route = StateRoute,
        label = "State",
        selectedIcon = Icons.Rounded.Tune,
        unselectedIcon = Icons.Outlined.Tune
    ),
    SideEffects(
        route = SideEffectsRoute,
        label = "Side Effects",
        selectedIcon = Icons.Rounded.Bolt,
        unselectedIcon = Icons.Outlined.Bolt
    )
}
