package android.ai.jetpackdemonstration.ui.components

import android.ai.jetpackdemonstration.ui.components.demos.ButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.FloatingActionButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.IconButtonDemoScreen
import android.ai.jetpackdemonstration.ui.components.demos.SegmentedButtonDemoScreen
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
        }
    }
}
