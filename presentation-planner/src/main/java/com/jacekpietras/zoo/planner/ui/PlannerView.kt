package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.reordering.ReorderingData
import com.jacekpietras.zoo.planner.reordering.dragOnLongPressToReorder
import com.jacekpietras.zoo.planner.reordering.getAdditionalOffset

@Composable
internal fun PlannerView(
    viewState: PlannerViewState,
    onRemove: (regionId: String) -> Unit,
    onUnlock: (regionId: String) -> Unit,
    onMove: (from: String, to: String) -> Unit,
    onAddExit: () -> Unit,
) {
    if (viewState.isEmptyViewVisible) {
        EmptyView()
    }

    Column {
        PlannerListView(
            modifier = Modifier.weight(1f),
            viewState = viewState,
            onMove = onMove,
            onRemove = onRemove,
            onUnlock = onUnlock,
        )
        if (viewState.isAddExitVisible) {
            SimpleButton(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.End),
                text = RichText("Add Exit"),
                onClick = onAddExit
            )
        }
    }

    TopGradientView()
}

@Composable
private fun TopGradientView() {
    val statusBarsPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colors.background,
                        MaterialTheme.colors.background,
                        Color.Transparent,
                    )
                )
            )
            .fillMaxWidth()
            .height(statusBarsPadding)
    )
}

@Composable
private fun PlannerListView(
    modifier: Modifier = Modifier,
    viewState: PlannerViewState,
    onMove: (from: String, to: String) -> Unit,
    onRemove: (regionId: String) -> Unit,
    onUnlock: (regionId: String) -> Unit,
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
                elevation = elevation,
                text = item.regionId + " (" + item.text.toString(LocalContext.current) + ")",
                onRemove = { onRemove(item.regionId) },
                onUnlock = { onUnlock(item.regionId) }
            )
        }
    }
}

private fun List<PlannerItem>.firstNotSeen(): Int =
    indexOfFirst { !it.isSeen }.minus(1).takeIf { it >= 0 } ?: 0

private fun Modifier.statusBarsPaddingWhen(condition: () -> Boolean): Modifier =
    if (condition()) {
        statusBarsPadding()
    } else {
        this
    }

@Composable
private fun SimpleButton(
    modifier: Modifier = Modifier,
    text: RichText,
    onClick: () -> Unit = {},
) =
    Button(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minHeight = 56.dp)
            .then(modifier),
        elevation = null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text.toString(LocalContext.current),
            style = MaterialTheme.typography.button,
            textAlign = TextAlign.Center,
        )
    }
