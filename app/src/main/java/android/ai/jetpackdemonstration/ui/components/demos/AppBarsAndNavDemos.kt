package android.ai.jetpackdemonstration.ui.components.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarDemoScreen(modifier: Modifier = Modifier) {
    val barTypes = listOf("Small", "Center", "Medium", "Large")
    var selectedType by remember { mutableIntStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            barTypes.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedType == index,
                    onClick = { selectedType = index },
                    label = { Text(label) }
                )
            }
        }

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                }
                val navIcon: @Composable () -> Unit = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }

                when (selectedType) {
                    0 -> TopAppBar(
                        title = { Text("Small TopAppBar") },
                        navigationIcon = navIcon,
                        actions = actions
                    )
                    1 -> CenterAlignedTopAppBar(
                        title = { Text("Center Aligned") },
                        navigationIcon = navIcon,
                        actions = actions
                    )
                    2 -> MediumTopAppBar(
                        title = { Text("Medium TopAppBar") },
                        navigationIcon = navIcon,
                        actions = actions,
                        scrollBehavior = scrollBehavior
                    )
                    else -> LargeTopAppBar(
                        title = { Text("Large TopAppBar") },
                        navigationIcon = navIcon,
                        actions = actions,
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(20) { index ->
                    ListItem(headlineContent = { Text("Scrollable Item ${index + 1}") })
                }
            }
        }
    }
}

@Composable
fun BottomAppBarDemoScreen(modifier: Modifier = Modifier) {
    var selectedAction by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { selectedAction = 1 }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                    IconButton(onClick = { selectedAction = 2 }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { selectedAction = 3 }) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { selectedAction = 4 },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selectedAction == 0) "Tap an action below" else "Action #$selectedAction tapped",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun NavigationBarDemoScreen(modifier: Modifier = Modifier) {
    val items = listOf("Home", "Favorites", "Profile", "Settings")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.Person, Icons.Filled.Settings)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.FavoriteBorder, Icons.Outlined.Person, Icons.Outlined.Settings)
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                                contentDescription = label
                            )
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = items[selectedItem],
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun NavigationRailDemoScreen(modifier: Modifier = Modifier) {
    val items = listOf("Home", "Favorites", "Settings")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.Settings)
    var selectedItem by remember { mutableIntStateOf(0) }

    Row(modifier = modifier.fillMaxSize()) {
        NavigationRail(
            header = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        ) {
            items.forEachIndexed { index, label ->
                NavigationRailItem(
                    selected = selectedItem == index,
                    onClick = { selectedItem = index },
                    icon = { Icon(icons[index], contentDescription = label) },
                    label = { Text(label) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = items[selectedItem],
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun NavigationDrawerDemoScreen(modifier: Modifier = Modifier) {
    val drawerItems = listOf("Home", "Favorites", "Profile", "Settings")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.Person, Icons.Filled.Settings)
    var selectedIndex by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Navigation Drawer",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                drawerItems.forEachIndexed { index, label ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = index == selectedIndex,
                        onClick = {
                            selectedIndex = index
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(icons[index], contentDescription = label) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selected: ${drawerItems[selectedIndex]}",
                style = MaterialTheme.typography.titleLarge
            )
            Button(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Open Drawer")
            }
        }
    }
}

@Composable
fun TabsDemoScreen(modifier: Modifier = Modifier) {
    val tabTypes = listOf("Primary", "Secondary", "Scrollable")
    var selectedType by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val shortTabs = listOf("Flights", "Hotels", "Trips")
    val longTabs = listOf("Overview", "Photos", "Reviews", "Prices", "Location", "Amenities")

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabTypes.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedType == index,
                    onClick = {
                        selectedType = index
                        selectedTab = 0
                    },
                    label = { Text(label) }
                )
            }
        }

        when (selectedType) {
            0 -> PrimaryTabRow(selectedTabIndex = selectedTab) {
                shortTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            1 -> SecondaryTabRow(selectedTabIndex = selectedTab) {
                shortTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            else -> PrimaryScrollableTabRow(selectedTabIndex = selectedTab) {
                longTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        val currentTabs = if (selectedType == 2) longTabs else shortTabs
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${currentTabs[selectedTab]} Content",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldDemoScreen(modifier: Modifier = Modifier) {
    var presses by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Scaffold TopAppBar") })
        },
        bottomBar = {
            BottomAppBar {
                Text(
                    text = "Scaffold BottomAppBar",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    presses++
                    scope.launch {
                        snackbarHostState.showSnackbar("FAB pressed $presses times")
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Scaffold Body (Presses: $presses)",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
