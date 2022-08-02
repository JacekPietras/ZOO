package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.R
import com.jacekpietras.zoo.planner.extensions.statusBarsPaddingWhen
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerItem.RegionItem
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.reordering.ReorderingData
import com.jacekpietras.zoo.planner.reordering.dragOnLongPressToReorder
import com.jacekpietras.zoo.planner.reordering.getAdditionalOffset

@Composable
internal fun PlannerListView(
    modifier: Modifier = Modifier,
    viewState: PlannerViewState,
    onMove: (from: String, to: String) -> Unit,
    onRemove: (regionId: String) -> Unit,
    onUnlock: (regionId: String) -> Unit,
    onUnsee: (regionId: String) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val reorderingData = remember { mutableStateOf<ReorderingData?>(null) }
    val listData by remember { mutableStateOf(viewState.list) }.also { it.value = viewState.list }

    LaunchedEffect("scroll" + listData.isNotEmpty()) {
        lazyListState.animateScrollToItem(listData.firstNotSeen())
    }

    LazyColumn(
        modifier = modifier,
//        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        state = lazyListState,
    ) {
        items(
            count = viewState.list.size,
            key = { viewState.list[it].hashCode() },
        ) { index ->
            when (val item = viewState.list[index]) {
                is RegionItem -> {
                    val isDragged = reorderingData.value?.fromIndex == index
                    val additionalOffset = reorderingData.value?.getAdditionalOffset(index = index, item.isFixed)

                    RegionCardView(
                        modifier = Modifier
                            .statusBarsPaddingWhen { index == 0 }
                            .dragOnLongPressToReorder(
                                isFixed = item.isFixed,
                                additionalOffset = additionalOffset,
                                index = index,
                                key = item.hashCode(),
                                lazyListState = lazyListState,
                                onOrderingChange = { reorderingData.value = it },
                                onDragStop = { fromIndex, toIndex ->
                                    onMove(
                                        (listData[fromIndex] as RegionItem).regionId,
                                        (listData[toIndex] as RegionItem).regionId,
                                    )
                                },
                            ),
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
                PlannerItem.UserPositionItem -> {
                    Row {
                        Icon(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .align(Alignment.CenterVertically),
                            painter = painterResource(id = R.drawable.ic_trip_ball_18),
                            tint = ZooTheme.colors.onSurface,
                            contentDescription = null // decorative element
                        )
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            text = "Your position",
                            style = MaterialTheme.typography.subtitle2,
                            color = ZooTheme.colors.textPrimaryOnSurface,
                        )
                    }
                }
            }
        }
    }
}

private fun List<PlannerItem>.firstNotSeen(): Int =
    indexOfFirst { it is RegionItem && !it.isSeen }.minus(1).takeIf { it >= 0 } ?: 0
