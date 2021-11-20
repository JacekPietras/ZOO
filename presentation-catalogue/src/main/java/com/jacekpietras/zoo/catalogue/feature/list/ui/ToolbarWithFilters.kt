package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision

@Composable
internal fun ToolbarWithFilters(
    filterList: List<AnimalDivision>,
    filtersVisible: Boolean,
    searchVisible: Boolean,
    searchText: String,
    onSearch: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onFilterClicked: (AnimalDivision) -> Unit,
) {
    Card(
        shape = RectangleShape,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 6.dp,
    ) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
        ) {
            AnimatedVisibility(
                visibleState = remember { MutableTransitionState(true) }
                    .apply { targetState = filtersVisible },
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AnimalDivision.values().forEach { animalDivision ->
                        ToolbarIcon(
                            id = animalDivision.iconRes,
                            selected = filterList.contains(animalDivision),
                            onClick = { onFilterClicked(animalDivision) },
                        )
                    }
                    ToolbarIcon(
                        id = R.drawable.ic_search_24,
                        onClick = onSearchClicked,
                    )
                }
            }
            AnimatedVisibility(
                visibleState = remember { MutableTransitionState(false) }
                    .apply { targetState = searchVisible },
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 }),
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        painter = painterResource(id = R.drawable.ic_search_24),
                        tint = MaterialTheme.colors.onSurface,
                        contentDescription = null // decorative element
                    )

                    SearchView(searchText, onSearch)

                    ToolbarIcon(
                        padding = 8.dp,
                        id = R.drawable.ic_close_24,
                        onClick = onSearchClicked,
                    )
                }
            }
        }
    }
}
