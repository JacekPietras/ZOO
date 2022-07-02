package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.utils.ReorderingData
import com.jacekpietras.zoo.planner.utils.dragOnLongPressToReorder
import com.jacekpietras.zoo.planner.utils.getAdditionalOffset

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
            viewState,
            onMove,
            onRemove,
            onUnlock
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
}

@Composable
private fun ColumnScope.PlannerListView(
    viewState: PlannerViewState,
    onMove: (from: String, to: String) -> Unit,
    onRemove: (regionId: String) -> Unit,
    onUnlock: (regionId: String) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val reorderingData = remember { mutableStateOf<ReorderingData?>(null) }
    val listData = remember { mutableStateOf(viewState.list) }
    listData.value = viewState.list

    LazyColumn(
        modifier = Modifier.weight(1f),
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
                    .dragOnLongPressToReorder(
                        isFixed = item.isFixed,
                        additionalOffset = additionalOffset,
                        index = index,
                        key = item.hashCode(),
                        lazyListState = lazyListState,
                        onOrderingChange = { reorderingData.value = it },
                        onDragStop = { fromIndex, toIndex ->
                            onMove(listData.value[fromIndex].regionId, listData.value[toIndex].regionId)
                        },
                    ),
                isMutable = item.isMutable,
                elevation = elevation,
                text = item.regionId + " (" + item.text.toString(LocalContext.current) + ")",
                onRemove = { onRemove(item.regionId) },
                onUnlock = { onUnlock(item.regionId) }
            )
        }
    }
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
