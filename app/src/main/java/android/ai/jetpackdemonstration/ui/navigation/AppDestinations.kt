package android.ai.jetpackdemonstration.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Components(
        route = "components",
        label = "Components",
        selectedIcon = Icons.Rounded.Widgets,
        unselectedIcon = Icons.Outlined.Widgets
    ),
    State(
        route = "state",
        label = "State",
        selectedIcon = Icons.Rounded.Tune,
        unselectedIcon = Icons.Outlined.Tune
    ),
    SideEffects(
        route = "side_effects",
        label = "Side Effects",
        selectedIcon = Icons.Rounded.Bolt,
        unselectedIcon = Icons.Outlined.Bolt
    )
}
