package android.ai.jetpackdemonstration.ui.components

import android.ai.jetpackdemonstration.ui.components.demos.BadgeDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.BottomAppBarDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.BottomSheetDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.ButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.CardDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.CarouselDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.CheckboxDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.ChipDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.DatePickerDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.DialogDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.DividerDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.FloatingActionButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.IconButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.LazyGridDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.LazyListsDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.ListItemDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.MenusDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.NavigationBarDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.NavigationDrawerDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.NavigationRailDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.PagerDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.ProgressIndicatorDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.PullToRefreshDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.RadioButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.ScaffoldDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.SearchBarDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.SegmentedButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.SliderDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.SnackbarDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.SwitchDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.TabsDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.TextFieldDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.TimePickerDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.TooltipDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.TopAppBarDemoScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDetailScreen(
    componentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val component = ComponentCatalog.findById(componentId)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(component?.name ?: "Component") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (componentId) {
            "button" -> ButtonDemoScreen(contentModifier)
            "icon_button" -> IconButtonDemoScreen(contentModifier)
            "fab" -> FloatingActionButtonDemoScreen(contentModifier)
            "segmented_button" -> SegmentedButtonDemoScreen(contentModifier)
            "top_app_bar" -> TopAppBarDemoScreen(contentModifier)
            "bottom_app_bar" -> BottomAppBarDemoScreen(contentModifier)
            "navigation_bar" -> NavigationBarDemoScreen(contentModifier)
            "navigation_rail" -> NavigationRailDemoScreen(contentModifier)
            "navigation_drawer" -> NavigationDrawerDemoScreen(contentModifier)
            "tabs" -> TabsDemoScreen(contentModifier)
            "scaffold" -> ScaffoldDemoScreen(contentModifier)
            "card" -> CardDemoScreen(contentModifier)
            "list_item" -> ListItemDemoScreen(contentModifier)
            "divider" -> DividerDemoScreen(contentModifier)
            "lazy_lists" -> LazyListsDemoScreen(contentModifier)
            "lazy_grid" -> LazyGridDemoScreen(contentModifier)
            "pager" -> PagerDemoScreen(contentModifier)
            "carousel" -> CarouselDemoScreen(contentModifier)
            "text_field" -> TextFieldDemoScreen(contentModifier)
            "search_bar" -> SearchBarDemoScreen(contentModifier)
            "checkbox" -> CheckboxDemoScreen(contentModifier)
            "radio_button" -> RadioButtonDemoScreen(contentModifier)
            "switch" -> SwitchDemoScreen(contentModifier)
            "slider" -> SliderDemoScreen(contentModifier)
            "chip" -> ChipDemoScreen(contentModifier)
            "menus" -> MenusDemoScreen(contentModifier)
            "dialog" -> DialogDemoScreen(contentModifier)
            "bottom_sheet" -> BottomSheetDemoScreen(contentModifier)
            "snackbar" -> SnackbarDemoScreen(contentModifier)
            "progress_indicator" -> ProgressIndicatorDemoScreen(contentModifier)
            "badge" -> BadgeDemoScreen(contentModifier)
            "tooltip" -> TooltipDemoScreen(contentModifier)
            "pull_to_refresh" -> PullToRefreshDemoScreen(contentModifier)
            "date_picker" -> DatePickerDemoScreen(contentModifier)
            "time_picker" -> TimePickerDemoScreen(contentModifier)
        }
    }
}
