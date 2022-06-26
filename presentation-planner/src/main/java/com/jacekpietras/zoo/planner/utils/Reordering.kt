package com.jacekpietras.zoo.planner.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

internal fun Modifier.dragOnLongPressToReorder(
    additionalOffset: Float,
    key: Any,
    lazyListState: LazyListState,
    dragChange: (ReorderingData?) -> Unit,
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
                dragChange(getReorderingData(lazyListState, key = key, offsetY.value))
            },
            onDragStart = {
                dragChange(null)
            },
            onDragEnd = {
                offsetY.value = 0f
                dragChange(null)
            },
            onDragCancel = {
                offsetY.value = 0f
                dragChange(null)
            }
        )
    }
        .graphicsLayer(translationY = animateOffset + animateAdditionalOffset)
        .zIndex(if (animateOffset != 0f) 1.0f else 0.0f)
}

private fun getReorderingData(lazyListState: LazyListState, key: Any, offset: Float): ReorderingData =
    with(lazyListState.layoutInfo) {
        val keyItem = visibleItemsInfo.firstOrNull { it.key == key }
        val keyOffset = keyItem?.offset ?: 0

        ReorderingData(
            firstIndexAfter = visibleItemsInfo.firstOrNull { it.offset > keyOffset + offset }?.index ?: 0,
            draggedIndex = keyItem?.index ?: 0,
            draggedHeight = visibleItemsInfo[keyItem?.index?.plus(1) ?: 0].offset - keyOffset,
        )
    }

internal fun ReorderingData?.getAdditionalOffset(index: Int): Int {
    if (this == null) {
        return 0
    }
    val positiveOffset =
        if (index > firstIndexAfter - 1 && index < draggedIndex) {
            draggedHeight
        } else {
            0
        }
    val negativeOffset =
        if (index in (draggedIndex + 1) until firstIndexAfter) {
            -draggedHeight
        } else {
            0
        }
    return positiveOffset + negativeOffset
}

internal class ReorderingData(
    val firstIndexAfter: Int = 0,
    val draggedIndex: Int = 0,
    val draggedHeight: Int = 0,
)