package com.jacekpietras.zoo.planner.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.planner.model.PlannerViewState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

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
        val lazyListState = rememberLazyListState()
        val firstIndexAfter = remember { mutableStateOf(0) }
        val draggedIndex = remember { mutableStateOf(0) }
        val draggedHeight = remember { mutableStateOf(0) }

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            state = lazyListState,
        ) {
            items(
                count = viewState.list.size,
                key = { viewState.list[it].hashCode() }
            ) { index ->
                val item = viewState.list[index]
                val isDragged = remember { mutableStateOf(false) }
                val elevation = if (isDragged.value) 8.dp else 4.dp
                Timber.e("dupa " + index + " " + firstIndexAfter.value + " " + draggedIndex.value)

                val positiveOffset =
                    if (index > firstIndexAfter.value - 1 && index < draggedIndex.value && firstIndexAfter.value < draggedIndex.value) {
                        draggedHeight.value
                    } else {
                        0
                    }
                val negativeOffset =
                    if (index < firstIndexAfter.value && index > draggedIndex.value && firstIndexAfter.value > draggedIndex.value) {
                        -draggedHeight.value
                    } else {
                        0
                    }

                RegionCardView(
                    modifier = Modifier
                        .dragOnLongPressToReorder(
                            additionalOffset = (positiveOffset + negativeOffset).toFloat(),
                        ) { isDragging, offset ->
                            isDragged.value = isDragging
                            val key = item.hashCode()

                            with(lazyListState.layoutInfo) {
                                val keyItem = visibleItemsInfo.firstOrNull { it.key == key }
                                draggedIndex.value = keyItem?.index ?: 0
                                val keyOffset = keyItem?.offset ?: 0
                                firstIndexAfter.value = visibleItemsInfo.firstOrNull { it.offset > keyOffset + offset }?.index ?: 0
                                draggedHeight.value = visibleItemsInfo[keyItem?.index?.plus(1) ?: 0].offset - (keyItem?.offset ?: 0)
                            }
                        },
                    elevation = elevation,
                    text = item.text.toString(LocalContext.current),
                    onRemove = { onRemove(item.regionId) },
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

fun Modifier.dragOnLongPressToReorder(
    additionalOffset: Float,
    dragChange: (Boolean, Float) -> Unit,
): Modifier = composed {
    val offsetY = remember { mutableStateOf(0f) }
    val animateAdditionalOffset by animateFloatAsState(targetValue = additionalOffset)
    val animateOffset by animateFloatAsState(targetValue = offsetY.value)

    pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDrag = { change, offset ->
                change.consume()
                // compute calculatedOffset
                offsetY.value += offset.y
                dragChange(true, offsetY.value)
            },
            onDragStart = {
                dragChange(true, 0f)
            },
            onDragEnd = {
                offsetY.value = 0f
                dragChange(false, 0f)
            },
            onDragCancel = {
                offsetY.value = 0f
                dragChange(false, 0f)
            }
        )
    }
        .graphicsLayer(translationY = animateOffset + animateAdditionalOffset)
        .zIndex(if (animateOffset != 0f) 1.0f else 0.0f)
}

fun Modifier.dragOnShortPressToReorder(
    dragChange: (Boolean) -> Unit = {},
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    pointerInput(Unit) {
        coroutineScope {
            while (true) {
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                offsetX.stop()
                offsetY.stop()
                awaitPointerEventScope {
                    drag(pointerId) { change ->
                        dragChange(true)
                        val horizontalDragOffset = offsetX.value + change.positionChange().x
                        launch {
                            offsetX.snapTo(horizontalDragOffset)
                        }
                        val verticalDragOffset = offsetY.value + change.positionChange().y
                        launch {
                            offsetY.snapTo(verticalDragOffset)
                        }
                        change.consume()
                    }
                }
                launch {
                    offsetX.animateTo(0f)
                }
                launch {
                    offsetY.animateTo(0f)
                    dragChange(false)
                }
            }
        }
    }.offset { IntOffset(offsetX.value.roundToInt() / 2, offsetY.value.roundToInt()) }
}
