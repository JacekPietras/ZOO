package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.R
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerItem.Footer
import com.jacekpietras.zoo.planner.model.PlannerItem.Header
import com.jacekpietras.zoo.planner.model.PlannerItem.RegionItem
import com.jacekpietras.zoo.planner.model.PlannerItem.UserPositionItem
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.model.SuggestedItem
import com.jacekpietras.zoo.planner.reordering.ReorderingData
import com.jacekpietras.zoo.planner.reordering.dragOnLongPressToReorder
import com.jacekpietras.zoo.planner.reordering.getAdditionalOffset

@Composable
internal fun PlannerListView(
    viewState: PlannerViewState,
    onMove: (from: String, to: String) -> Unit,
    onRemove: (regionId: String) -> Unit,
    onUnlock: (regionId: String) -> Unit,
    onUnsee: (regionId: String) -> Unit,
    onSuggestedItemClicked: (suggestedItem: SuggestedItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val lazyListState = rememberLazyListState()
        val reorderingData = remember { mutableStateOf<ReorderingData?>(null) }
        val listData by remember { mutableStateOf(viewState.list) }.also { it.value = viewState.list }
        val firstRegion = viewState.list.indexOfFirst { it is RegionItem || it is UserPositionItem }
        val lastRegion = viewState.list.indexOfLast { it is RegionItem || it is UserPositionItem }
        val lastEnd = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.let { it.offset + it.size } ?: 0
        val sparePlace = lazyListState.layoutInfo.viewportEndOffset - lastEnd
        val fixedFooter = sparePlace > 0

        LaunchedEffect("scroll" + listData.isNotEmpty()) {
            lazyListState.animateScrollToItem(listData.firstNotSeen())
        }

        LazyColumn(
            modifier = Modifier.weight(1f, true),
            state = lazyListState,
        ) {
            items(
                count = viewState.list.size,
                key = { viewState.list[it].key },
            ) { index ->
                when (val item = viewState.list[index]) {
                    is Header -> {
                        HeaderView()
                    }
                    is UserPositionItem -> {
                        UserPositionView()
                    }
                    is RegionItem -> {
                        val moveFrom = reorderingData.value?.fromIndex
                        val isDragged = moveFrom == index
                        val additionalOffset = reorderingData.value?.getAdditionalOffset(index = index, item.isFixed)

                        val moveTo = reorderingData.value?.toIndex
                        val isFirst = index == firstRegion || if (index < (moveFrom ?: -1)) moveTo == index else moveTo == index - 1
                        val isLast = index == lastRegion || if (index < (moveFrom ?: -1)) moveTo == index + 1 else moveTo == index

                        RegionCardView(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .dragOnLongPressToReorder(
                                    isFixed = item.isFixed,
                                    additionalOffset = additionalOffset,
                                    index = index,
                                    key = item.key,
                                    lazyListState = lazyListState,
                                    onOrderingChange = { reorderingData.value = it },
                                    onDragStop = { fromIndex, toIndex ->
                                        val from = listData[fromIndex]
                                        val to = listData[toIndex]
                                        if (from is RegionItem && to is RegionItem) {
                                            onMove(
                                                from.regionId,
                                                to.regionId,
                                            )
                                        }
                                    },
                                ),
                            key = item.key,
                            isFirst = isFirst,
                            isLast = isLast,
                            isMutable = item.isMutable,
                            isSeen = item.isSeen,
                            isRemovable = item.isRemovable,
                            isDragged = isDragged,
                            title = item.title,
                            info = item.info,
                            onRemove = { onRemove(item.regionId) },
                            onUnlock = { onUnlock(item.regionId) },
                            onUnsee = { onUnsee(item.regionId) },
                        )
                    }
                    is Footer -> {
                        if (!fixedFooter) {
                            FooterView(
                                items = viewState.suggestedItems,
                                onSuggestedItemClicked = onSuggestedItemClicked,
                            )
                        }
                    }
                }
            }
        }

        if (fixedFooter && viewState.list.any { it is Footer }) {
            FooterView(
                items = viewState.suggestedItems,
                onSuggestedItemClicked = onSuggestedItemClicked,
            )
        }
    }
}

@Composable
private fun HeaderView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 16.dp)
            .background(ZooTheme.colors.surfaceSecondary)
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.planner_title),
            style = MaterialTheme.typography.h5,
            color = ZooTheme.colors.textPrimaryOnSurface,
        )
        Text(
            text = stringResource(id = R.string.planner_description),
            style = MaterialTheme.typography.body1,
            color = ZooTheme.colors.textSecondaryOnSurface,
        )
    }
}

@Composable
private fun FooterView(
    modifier: Modifier = Modifier,
    items: List<SuggestedItem>,
    onSuggestedItemClicked: (suggestedItem: SuggestedItem) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ZooTheme.colors.surfaceSecondary)
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(id = R.string.planner_suggestion_title),
            style = MaterialTheme.typography.subtitle1,
            color = ZooTheme.colors.textPrimaryOnSurface,
        )
        items.forEach { item ->
            SuggestionButton(
                text = item.text,
                onClick = { onSuggestedItemClicked(item) }
            )
        }
    }
}

@Composable
private fun UserPositionView() {
    Box {
        Divider(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 56.dp),
            color = ZooTheme.colors.primary,
            thickness = 2.dp,
        )
        Row {
            Icon(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.ic_trip_ball_18),
                tint = ZooTheme.colors.primary,
                contentDescription = null // decorative element
            )
        }
    }
}

private fun List<PlannerItem>.firstNotSeen(): Int =
    if (any { it is RegionItem && it.isSeen }) {
        indexOfFirst { it is RegionItem && !it.isSeen }.minus(1).takeIf { it >= 0 } ?: 0
    } else {
        0
    }