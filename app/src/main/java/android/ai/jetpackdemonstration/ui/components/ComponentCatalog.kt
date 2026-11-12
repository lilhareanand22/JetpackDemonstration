package android.ai.jetpackdemonstration.ui.components

import android.ai.jetpackdemonstration.ui.components.model.ComponentCategory
import android.ai.jetpackdemonstration.ui.components.model.ComponentItem

object ComponentCatalog {
    val items: List<ComponentItem> = listOf(
        ComponentItem(
            id = "button",
            name = "Button",
            category = ComponentCategory.ButtonsAndActions
        ),
        ComponentItem(
            id = "icon_button",
            name = "IconButton",
            category = ComponentCategory.ButtonsAndActions
        ),
        ComponentItem(
            id = "fab",
            name = "FloatingActionButton",
            category = ComponentCategory.ButtonsAndActions
        ),
        ComponentItem(
            id = "segmented_button",
            name = "SegmentedButton",
            category = ComponentCategory.ButtonsAndActions
        ),
        ComponentItem(
            id = "top_app_bar",
            name = "TopAppBar",
            category = ComponentCategory.AppBarsAndNavigation
        ),
        ComponentItem(
            id = "bottom_app_bar",
            name = "BottomAppBar",
            category = ComponentCategory.AppBarsAndNavigation
        ),
        ComponentItem(
            id = "navigation_bar",
            name = "NavigationBar",
            category = ComponentCategory.AppBarsAndNavigation
        ),
        ComponentItem(
            id = "navigation_rail",
            name = "NavigationRail",
            category = ComponentCategory.AppBarsAndNavigation
        ),
        ComponentItem(
            id = "navigation_drawer",
            name = "ModalNavigationDrawer",
            category = ComponentCategory.AppBarsAndNavigation
        ),
        ComponentItem(
            id = "tabs",
            name = "Tabs",
            category = ComponentCategory.AppBarsAndNavigation
        ),
        ComponentItem(
            id = "scaffold",
            name = "Scaffold",
            category = ComponentCategory.AppBarsAndNavigation
        )
    )

    fun findById(id: String): ComponentItem? = items.find { it.id == id }
}
