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
        )
    )

    fun findById(id: String): ComponentItem? = items.find { it.id == id }
}
