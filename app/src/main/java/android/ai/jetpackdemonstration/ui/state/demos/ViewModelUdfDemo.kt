package android.ai.jetpackdemonstration.ui.state.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── 1. IMMUTABLE UI STATE (SEALED INTERFACE) ─────────────────────────

sealed interface ArticlesUiState {
    data object Loading : ArticlesUiState
    data class Success(
        val articles: List<String>,
        val isRefreshing: Boolean = false
    ) : ArticlesUiState
    data class Error(val errorMessage: String) : ArticlesUiState
}

// ─── 2. USER ACTIONS (SEALED INTERFACE / EVENTS) ──────────────────────

sealed interface ArticleUserAction {
    data object Refresh : ArticleUserAction
    data class DeleteArticle(val title: String) : ArticleUserAction
    data object TriggerSimulatedError : ArticleUserAction
}

// ─── 3. VIEWMODEL (SINGLE SOURCE OF TRUTH) ───────────────────────────

class ArticleViewModel : ViewModel() {

    // Backing private mutable state flow
    private val _uiState = MutableStateFlow<ArticlesUiState>(ArticlesUiState.Loading)

    // Public immutable state flow exposed to the UI
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    init {
        loadInitialArticles()
    }

    private fun loadInitialArticles() {
        viewModelScope.launch {
            _uiState.value = ArticlesUiState.Loading
            delay(1200) // Simulate network latency
            _uiState.value = ArticlesUiState.Success(
                articles = listOf(
                    "Understanding Jetpack Compose Snapshot System",
                    "Unidirectional Data Flow in Modern Android",
                    "StateFlow vs SharedFlow: Architectural Guide",
                    "Lifecycle-Aware State Collection with Compose"
                )
            )
        }
    }

    // Single entry point for processing all UI events (MVI / UDF principle)
    fun onAction(action: ArticleUserAction) {
        when (action) {
            is ArticleUserAction.Refresh -> refreshArticles()
            is ArticleUserAction.DeleteArticle -> deleteArticle(action.title)
            is ArticleUserAction.TriggerSimulatedError -> simulateError()
        }
    }

    private fun refreshArticles() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ArticlesUiState.Success) {
                // Show subtle refreshing state without wiping current list
                _uiState.update { currentState.copy(isRefreshing = true) }
            } else {
                _uiState.value = ArticlesUiState.Loading
            }

            delay(1000)

            _uiState.value = ArticlesUiState.Success(
                articles = listOf(
                    "Understanding Jetpack Compose Snapshot System",
                    "Unidirectional Data Flow in Modern Android",
                    "StateFlow vs SharedFlow: Architectural Guide",
                    "Lifecycle-Aware State Collection with Compose",
                    "New Article #${(10..99).random()}: Jetpack Compose 2026"
                )
            )
        }
    }

    private fun deleteArticle(title: String) {
        _uiState.update { current ->
            if (current is ArticlesUiState.Success) {
                current.copy(articles = current.articles.filter { it != title })
            } else current
        }
    }

    private fun simulateError() {
        _uiState.value = ArticlesUiState.Error(
            errorMessage = "HTTP 503: Service temporarily unavailable. Tap Retry."
        )
    }
}

// ─── 4. STATEFUL HOST (WIRES VIEWMODEL & LIFECYCLE) ───────────────────

@Composable
fun ViewModelUdfDemo(
    modifier: Modifier = Modifier,
    viewModel: ArticleViewModel = viewModel()
) {
    // ✅ collectAsStateWithLifecycle cancels collection when app is in background (STOPPED),
    // saving CPU, battery, and preventing background state emission crashes!
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Decoupled stateless screen receives immutable state & emits actions
    ArticleScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

// ─── 5. STATELESS COMPOSABLE (PURE UI, PREVIEWABLE, TESTABLE) ─────────

@Composable
fun ArticleScreenContent(
    uiState: ArticlesUiState,
    onAction: (ArticleUserAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section info header
        SectionHeader(
            number = "1",
            title = "Unidirectional Data Flow (UDF) Architecture",
            subtitle = "ViewModel owns StateFlow. UI sends Actions UP ↑ and collects immutable State DOWN ↓."
        )

        // Action Toolbar
        DemoCard(label = "UI Actions (Dispatched to ViewModel)") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAction(ArticleUserAction.Refresh) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Refresh", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onAction(ArticleUserAction.TriggerSimulatedError) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Simulate Error", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // State Renderer Card
        DemoCard(label = "Rendered UI State (from StateFlow)") {
            when (val state = uiState) {
                is ArticlesUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Loading articles from repository...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ArticlesUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Error Occurred",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { onAction(ArticleUserAction.Refresh) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry Fetch")
                        }
                    }
                }

                is ArticlesUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Articles (${state.articles.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.isRefreshing) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.size(6.dp))
                                    Text("Updating...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        if (state.articles.isEmpty()) {
                            Text(
                                text = "All articles deleted. Tap 'Refresh' above to reload.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            state.articles.forEach { title ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.size(8.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    IconButton(
                                        onClick = { onAction(ArticleUserAction.DeleteArticle(title)) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete article",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── SHARED UI HELPERS ──────────────────────────────────────────────

@Composable
private fun SectionHeader(number: String, title: String, subtitle: String) {
    Column {
        Text(
            text = "Section $number — $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DemoCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}
