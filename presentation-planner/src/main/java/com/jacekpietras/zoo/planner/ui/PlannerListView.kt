package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.planner.extensions.statusBarsPaddingWhen
import com.jacekpietras.zoo.planner.model.PlannerItem
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        state = lazyListState,
    ) {
        items(
            count = viewState.list.size,
            key = { viewState.list[it].hashCode() },
        ) { index ->
            val item = viewState.list[index]
            val elevation = if (reorderingData.value?.fromIndex == index) 8.dp else 4.dp
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
                            onMove(listData[fromIndex].regionId, listData[toIndex].regionId)
                        },
                    ),
                isMutable = item.isMutable,
                isSeen = item.isSeen,
                isRemovable = item.isRemovable,
                elevation = elevation,
                text = item.regionId + " (" + item.text.toString(LocalContext.current) + ")",
                onRemove = { onRemove(item.regionId) },
                onUnlock = { onUnlock(item.regionId) },
                onUnsee = { onUnsee(item.regionId) },
            )
        }
    }
}

private fun List<PlannerItem>.firstNotSeen(): Int =
    indexOfFirst { !it.isSeen }.minus(1).takeIf { it >= 0 } ?: 0
