package com.jacekpietras.zoo.planner.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
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
    isFixed: Boolean,
    additionalOffset: Int,
    key: Any,
    lazyListState: LazyListState,
    onOrderingChange: (ReorderingData?) -> Unit,
    onDragStop: (from: Int, to: Int) -> Unit,
): Modifier = composed {
    if (isFixed) return@composed this

    val offsetY = remember { mutableStateOf(0f) }
    val animateAdditionalOffset by animateIntAsState(targetValue = additionalOffset)
    val animateOffset by animateFloatAsState(targetValue = offsetY.value)

    pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDrag = { change, offset ->
                change.consume()
                offsetY.value += offset.y
                onOrderingChange(getReorderingData(lazyListState, key = key, offsetY.value))
            },
            onDragStart = {
                onOrderingChange(getReorderingData(lazyListState, key = key, 0f))
            },
            onDragEnd = {
                val reorderingData = getReorderingData(lazyListState, key = key, offsetY.value)
                if (reorderingData.fromIndex != reorderingData.toIndex) {
                    onDragStop(reorderingData.fromIndex, reorderingData.toIndex)
                }

                offsetY.value = 0f
                onOrderingChange(null)
            },
            onDragCancel = {
                offsetY.value = 0f
                onOrderingChange(null)
            }
        )
    }
        .graphicsLayer(translationY = animateOffset + animateAdditionalOffset)
        .zIndex(if (animateOffset != 0f) 1.0f else 0.0f)
}

private fun getReorderingData(lazyListState: LazyListState, key: Any, offset: Float): ReorderingData =
    with(lazyListState.layoutInfo) {
        val keyItem = visibleItemsInfo.firstOrNull { it.key == key }
        val keyIndexInList = visibleItemsInfo.indexOf(keyItem)
        val keyOffset = keyItem?.offset ?: 0
        val keyIndex = keyItem?.index ?: 0
        val height = if (keyIndexInList + 1 < visibleItemsInfo.size) {
            visibleItemsInfo[keyIndexInList + 1].offset - keyOffset
        } else if (keyIndexInList > 0) {
            keyOffset - visibleItemsInfo[keyIndexInList - 1].offset
        } else {
            0
        }
        val firstAfter = visibleItemsInfo.firstOrNull { it.offset > keyOffset + offset }?.index
            ?: if (visibleItemsInfo.last().offset > keyOffset + offset) {
                visibleItemsInfo.size
            } else {
                visibleItemsInfo.size + 1
            }

        val toIndex = if (keyIndex < firstAfter) {
            firstAfter - 1
        } else {
            firstAfter
        }.coerceIn(0..visibleItemsInfo.last().index)

        ReorderingData(
            toIndex = toIndex,
            fromIndex = keyIndex,
            draggedHeight = height,
        )
    }

internal fun ReorderingData?.getAdditionalOffset(index: Int, isFixed: Boolean): Int {
    if (this == null || isFixed) return 0

    val positiveOffset = if (index in toIndex until fromIndex) draggedHeight else 0
    val negativeOffset = if (index in (fromIndex + 1)..toIndex) -draggedHeight else 0

    return positiveOffset + negativeOffset
}

internal class ReorderingData(
    val fromIndex: Int = 0,
    val toIndex: Int = 0,
    val draggedHeight: Int = 0,
)
