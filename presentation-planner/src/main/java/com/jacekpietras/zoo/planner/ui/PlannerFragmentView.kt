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
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.utils.ReorderingData
import com.jacekpietras.zoo.planner.utils.dragOnLongPressToReorder
import com.jacekpietras.zoo.planner.utils.getAdditionalOffset

@Composable
internal fun PlannerFragmentView(
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
        val lazyListState = rememberLazyListState()
        val reorderingData = remember { mutableStateOf<ReorderingData?>(null) }

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            state = lazyListState,
        ) {
            items(
                count = viewState.list.size,
                key = { viewState.list[it].hashCode() },
            ) { index ->
                val item = viewState.list[index]
                val elevation = if (reorderingData.value?.draggedIndex == index) 8.dp else 4.dp
                val additionalOffset = reorderingData.value.getAdditionalOffset(index = index, item.isFixed)

                RegionCardView(
                    modifier = Modifier
                        .dragOnLongPressToReorder(
                            isFixed = item.isFixed,
                            additionalOffset = additionalOffset,
                            key = item.hashCode(),
                            lazyListState = lazyListState,
                            onOrderingChange = { reorderingData.value = it },
                            onDragStop = { fromIndex, toIndex ->
                                val from = viewState.list[fromIndex]
                                val to = viewState.list[toIndex.coerceAtMost(viewState.list.size - 1)]
                                onMove(from.regionId, to.regionId)
                            },
                        ),
                    isMutable = item.isMutable,
                    elevation = elevation,
                    text = item.text.toString(LocalContext.current),
                    onRemove = { onRemove(item.regionId) },
                    onUnlock = { onUnlock(item.regionId) }
                )
            }
        }
        if (viewState.isAddExitVisible) {
            SimpleButton(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.End),
                text = Text("Add Exit"),
                onClick = onAddExit
            )
        }
    }
}

@Composable
private fun SimpleButton(
    modifier: Modifier = Modifier,
    text: Text,
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
