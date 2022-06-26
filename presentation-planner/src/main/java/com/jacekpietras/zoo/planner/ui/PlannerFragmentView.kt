package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.planner.model.PlannerViewState
import org.burnoutcrew.reorderable.*
import com.jacekpietras.zoo.core.text.Text as RichText

@Composable
internal fun PlannerFragmentView(
    viewState: PlannerViewState,
    onRemove: (regionId: String) -> Unit,
    onAddExit: () -> Unit,
) {
    if (viewState.isEmptyViewVisible) {
        EmptyView()
    }
    Column {
        val data = remember { mutableStateOf(viewState.list) }
        data.value = viewState.list

        val state = rememberReorderableLazyListState(
            onMove = { from, to ->
//                if(data.value[to.index].isMutable) {
                data.value = data.value.toMutableList()
                    .apply { add(to.index, removeAt(from.index)) }
//                }
            })
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .weight(1f)
                .reorderable(state)
                .detectReorderAfterLongPress(state),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            items(data.value, { it.hashCode() }) { item ->
                ConditionalReorderableItem(state, key = item.hashCode(), condition = item.isMutable) { isDragged ->
                    val elevation = if (isDragged) 8.dp else 4.dp
                    RegionCardView(
                        elevation = elevation,
                        text = item.text.toString(LocalContext.current),
                        onRemove = { onRemove(item.regionId) },
                    )
                }
            }
        }
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
private fun ConditionalReorderableItem(
    state: ReorderableLazyListState,
    key: Any,
    condition: Boolean,
    content: @Composable (isDragging: Boolean) -> Unit
) {
    if (condition) {
        ReorderableItem(state, key = key) { isDragging ->
            content(isDragging)
        }
    } else {
        content(false)
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
