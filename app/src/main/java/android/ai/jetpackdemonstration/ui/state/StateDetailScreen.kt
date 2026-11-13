package android.ai.jetpackdemonstration.ui.state

import android.ai.jetpackdemonstration.ui.state.demos.ObservableStateCollectionsDemo
import android.ai.jetpackdemonstration.ui.state.demos.RecompositionScopeDemo
import android.ai.jetpackdemonstration.ui.state.demos.RememberVsRememberSaveableDemo
import android.ai.jetpackdemonstration.ui.state.demos.StateHoistingDemo
import android.ai.jetpackdemonstration.ui.state.demos.ViewModelUdfDemo
import android.ai.jetpackdemonstration.ui.state.model.StateTopicCatalog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateDetailScreen(
    topicId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topic = StateTopicCatalog.findById(topicId)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(topic?.title ?: "State Demo") },
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
        when (topicId) {
            "state_hoisting" -> StateHoistingDemo(contentModifier)
            "remember_saveable" -> RememberVsRememberSaveableDemo(contentModifier)
            "state_collections" -> ObservableStateCollectionsDemo(contentModifier)
            "viewmodel_udf" -> ViewModelUdfDemo(contentModifier)
            "recomposition_scope" -> RecompositionScopeDemo(contentModifier)
            else -> {
                Box(
                    modifier = contentModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Coming soon",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
