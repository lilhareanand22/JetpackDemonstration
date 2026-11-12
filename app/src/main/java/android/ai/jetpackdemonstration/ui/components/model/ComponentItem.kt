package android.ai.jetpackdemonstration.ui.components.model

enum class ComponentCategory(val title: String) {
    ButtonsAndActions("Buttons & Actions"),
    AppBarsAndNavigation("App Bars & Navigation"),
    CardsAndLists("Cards, Lists & Dividers"),
    InputsAndSelection("Inputs & Selection"),
    DialogsAndFeedback("Dialogs, Sheets & Feedback"),
    DateAndTime("Date & Time Pickers"),
    LayoutsAndCore("Layouts, Text & Media")
}

data class ComponentItem(
    val id: String,
    val name: String,
    val category: ComponentCategory
)
