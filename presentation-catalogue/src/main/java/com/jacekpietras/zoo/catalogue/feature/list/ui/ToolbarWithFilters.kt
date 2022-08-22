package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.core.theme.ZooTheme

@Composable
internal fun ToolbarWithFilters(
    filter: AnimalDivision?,
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
        elevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconsView(
                filtersVisible = filtersVisible,
                filter = filter,
                searchVisible = searchVisible,
                searchText = searchText,
                onFilterClicked = onFilterClicked,
                onSearchClicked = onSearchClicked,
                onSearch = onSearch,
            )

            DivisionNameView(filter)
        }
    }
}

@Composable
private fun ColumnScope.DivisionNameView(filter: AnimalDivision?) {
    var lastName by remember { mutableStateOf<Int?>(null) }

    AnimatedVisibility(
        modifier = Modifier.align(CenterHorizontally),
        visibleState = remember { MutableTransitionState(false) }
            .apply { targetState = filter != null },
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val name = filter?.nameRes?.also { lastName = it }
            ?: lastName

        Text(
            modifier = Modifier.padding(4.dp),
            text = name?.let { stringResource(it) } ?: "",
            style = MaterialTheme.typography.subtitle1,
            color = ZooTheme.colors.textPrimaryOnSurface,
        )
    }
}

@Composable
private fun IconsView(
    filtersVisible: Boolean,
    filter: AnimalDivision?,
    searchVisible: Boolean,
    searchText: String,
    onFilterClicked: (AnimalDivision) -> Unit,
    onSearchClicked: () -> Unit,
    onSearch: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .statusBarsPadding()
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
                        selected = filter == animalDivision,
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
