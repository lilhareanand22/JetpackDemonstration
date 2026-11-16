package android.ai.jetpackdemonstration.ui.sideeffects

import android.ai.jetpackdemonstration.ui.sideeffects.demos.DisposableEffectDemo
import android.ai.jetpackdemonstration.ui.sideeffects.demos.LaunchedEffectDemo
import android.ai.jetpackdemonstration.ui.sideeffects.demos.ProduceStateDemo
import android.ai.jetpackdemonstration.ui.sideeffects.demos.RememberCoroutineScopeDemo
import android.ai.jetpackdemonstration.ui.sideeffects.demos.RememberUpdatedStateDemo
import android.ai.jetpackdemonstration.ui.sideeffects.demos.SideEffectDemo
import android.ai.jetpackdemonstration.ui.sideeffects.demos.SnapshotFlowDemo
import android.ai.jetpackdemonstration.ui.sideeffects.model.SideEffectTopicCatalog
import android.ai.jetpackdemonstration.ui.state.demos.DerivedStateDemo
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
fun SideEffectDetailScreen(
    topicId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topic = SideEffectTopicCatalog.findById(topicId)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(topic?.title ?: "Side Effect Demo") },
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
            "launched_effect" -> LaunchedEffectDemo(contentModifier)
            "remember_coroutine_scope" -> RememberCoroutineScopeDemo(contentModifier)
            "remember_updated_state" -> RememberUpdatedStateDemo(contentModifier)
            "disposable_effect" -> DisposableEffectDemo(contentModifier)
            "side_effect" -> SideEffectDemo(contentModifier)
            "produce_state" -> ProduceStateDemo(contentModifier)
            "snapshot_flow" -> SnapshotFlowDemo(contentModifier)
            "derived_state" -> DerivedStateDemo(contentModifier)
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
