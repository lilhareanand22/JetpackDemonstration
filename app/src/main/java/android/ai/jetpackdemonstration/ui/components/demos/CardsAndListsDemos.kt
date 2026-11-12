package android.ai.jetpackdemonstration.ui.components.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun CardDemoScreen(modifier: Modifier = Modifier) {
    var clicks by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Filled Card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Elevated Card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Outlined Card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        Card(
            onClick = { clicks++ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Clickable Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tapped $clicks times",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ListItemDemoScreen(modifier: Modifier = Modifier) {
    var checked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ListItem(
            headlineContent = { Text("One-line ListItem") },
            leadingContent = {
                Icon(Icons.Filled.Person, contentDescription = null)
            }
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text("Two-line ListItem") },
            supportingContent = { Text("Supporting secondary text") },
            leadingContent = {
                Icon(Icons.Filled.Favorite, contentDescription = null)
            },
            trailingContent = {
                Checkbox(checked = checked, onCheckedChange = { checked = it })
            }
        )
        HorizontalDivider()

        ListItem(
            overlineContent = { Text("OVERLINE") },
            headlineContent = { Text("Three-line ListItem") },
            supportingContent = {
                Text("Longer supporting text that wraps across multiple lines in the item.")
            },
            leadingContent = {
                Icon(Icons.Filled.Star, contentDescription = null)
            },
            trailingContent = { Text("10:30") }
        )
        HorizontalDivider()
    }
}

@Composable
fun DividerDemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Item Above", style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider()
        Text("Item Below (Default 1.dp)", style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Text("After Thick Primary Divider", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Left")
            VerticalDivider()
            Text("Center")
            VerticalDivider(
                thickness = 3.dp,
                color = MaterialTheme.colorScheme.secondary
            )
            Text("Right")
        }
    }
}

@Composable
fun LazyListsDemoScreen(modifier: Modifier = Modifier) {
    val rowItems = remember { (1..15).map { "Chip $it" } }
    val colItems = remember { (1..30).map { "Row Item #$it" } }

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rowItems, key = { it }) { item ->
                ElevatedCard {
                    Text(
                        text = item,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colItems, key = { it }) { item ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LazyGridDemoScreen(modifier: Modifier = Modifier) {
    var fixedColumns by remember { mutableStateOf(false) }
    val gridItems = remember { (1..24).map { "Tile $it" } }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !fixedColumns,
                onClick = { fixedColumns = false },
                label = { Text("Adaptive (110.dp)") }
            )
            FilterChip(
                selected = fixedColumns,
                onClick = { fixedColumns = true },
                label = { Text("Fixed (3 columns)") }
            )
        }

        LazyVerticalGrid(
            columns = if (fixedColumns) GridCells.Fixed(3) else GridCells.Adaptive(110.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(gridItems, key = { it }) { title ->
                Card(
                    modifier = Modifier.height(90.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun PagerDemoScreen(modifier: Modifier = Modifier) {
    var isVertical by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 5 })

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isVertical,
                onClick = { isVertical = false },
                label = { Text("HorizontalPager") }
            )
            FilterChip(
                selected = isVertical,
                onClick = { isVertical = true },
                label = { Text("VerticalPager") }
            )
        }

        if (isVertical) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                PagerPageCard(page = page)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                PagerPageCard(page = page)
            }
        }

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pagerState.pageCount) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (active) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun PagerPageCard(page: Int) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Page ${page + 1}",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselDemoScreen(modifier: Modifier = Modifier) {
    val items = remember { (1..8).map { "Item $it" } }
    val multiBrowseState = rememberCarouselState { items.count() }
    val uncontainedState = rememberCarouselState { items.count() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "HorizontalMultiBrowseCarousel",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalMultiBrowseCarousel(
            state = multiBrowseState,
            preferredItemWidth = 186.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { index ->
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = items[index],
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = "HorizontalUncontainedCarousel",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalUncontainedCarousel(
            state = uncontainedState,
            itemWidth = 186.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { index ->
            Box(
                modifier = Modifier
                    .width(186.dp)
                    .height(180.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = items[index],
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
