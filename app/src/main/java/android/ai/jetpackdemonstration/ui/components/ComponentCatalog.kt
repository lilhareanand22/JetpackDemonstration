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
        ),
        ComponentItem(
            id = "card",
            name = "Card",
            category = ComponentCategory.CardsAndLists
        ),
        ComponentItem(
            id = "list_item",
            name = "ListItem",
            category = ComponentCategory.CardsAndLists
        ),
        ComponentItem(
            id = "divider",
            name = "Divider",
            category = ComponentCategory.CardsAndLists
        ),
        ComponentItem(
            id = "lazy_lists",
            name = "LazyColumn & LazyRow",
            category = ComponentCategory.CardsAndLists
        ),
        ComponentItem(
            id = "lazy_grid",
            name = "LazyVerticalGrid",
            category = ComponentCategory.CardsAndLists
        ),
        ComponentItem(
            id = "pager",
            name = "HorizontalPager & VerticalPager",
            category = ComponentCategory.CardsAndLists
        ),
        ComponentItem(
            id = "carousel",
            name = "Carousel",
            category = ComponentCategory.CardsAndLists
        ),
        ComponentItem(
            id = "text_field",
            name = "TextField & OutlinedTextField",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "search_bar",
            name = "SearchBar",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "checkbox",
            name = "Checkbox & TriStateCheckbox",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "radio_button",
            name = "RadioButton",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "switch",
            name = "Switch",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "slider",
            name = "Slider & RangeSlider",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "chip",
            name = "Chip",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "menus",
            name = "DropdownMenu & ExposedDropdownMenu",
            category = ComponentCategory.InputsAndSelection
        ),
        ComponentItem(
            id = "dialog",
            name = "AlertDialog & Dialog",
            category = ComponentCategory.DialogsAndFeedback
        ),
        ComponentItem(
            id = "bottom_sheet",
            name = "ModalBottomSheet",
            category = ComponentCategory.DialogsAndFeedback
        ),
        ComponentItem(
            id = "snackbar",
            name = "Snackbar",
            category = ComponentCategory.DialogsAndFeedback
        ),
        ComponentItem(
            id = "progress_indicator",
            name = "ProgressIndicator",
            category = ComponentCategory.DialogsAndFeedback
        ),
        ComponentItem(
            id = "badge",
            name = "Badge & BadgedBox",
            category = ComponentCategory.DialogsAndFeedback
        ),
        ComponentItem(
            id = "tooltip",
            name = "TooltipBox",
            category = ComponentCategory.DialogsAndFeedback
        ),
        ComponentItem(
            id = "pull_to_refresh",
            name = "PullToRefreshBox",
            category = ComponentCategory.DialogsAndFeedback
        ),
        ComponentItem(
            id = "date_picker",
            name = "DatePicker & DateRangePicker",
            category = ComponentCategory.DateAndTime
        ),
        ComponentItem(
            id = "time_picker",
            name = "TimePicker & TimeInput",
            category = ComponentCategory.DateAndTime
        )
    )

    fun findById(id: String): ComponentItem? = items.find { it.id == id }
}
