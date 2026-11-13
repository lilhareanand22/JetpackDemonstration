package android.ai.jetpackdemonstration.ui.navigation

import android.ai.jetpackdemonstration.ui.components.ComponentDetailScreen
import android.ai.jetpackdemonstration.ui.components.ComponentsScreen
import android.ai.jetpackdemonstration.ui.sideeffects.SideEffectsScreen
import android.ai.jetpackdemonstration.ui.state.StateDetailScreen
import android.ai.jetpackdemonstration.ui.state.StateScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun JetpackDemoApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = TopLevelDestination.entries.any { destination ->
        currentDestination?.hasRoute(destination.route::class) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(destination.route::class)
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ComponentsRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<ComponentsRoute> {
                ComponentsScreen(
                    onComponentClick = { componentId ->
                        navController.navigate(ComponentDetailRoute(componentId))
                    }
                )
            }
            composable<StateRoute> {
                StateScreen(
                    onTopicClick = { topicId ->
                        navController.navigate(StateDetailRoute(topicId))
                    }
                )
            }
            composable<SideEffectsRoute> {
                SideEffectsScreen()
            }
            composable<ComponentDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ComponentDetailRoute>()
                ComponentDetailScreen(
                    componentId = route.componentId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<StateDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<StateDetailRoute>()
                StateDetailScreen(
                    topicId = route.topicId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
