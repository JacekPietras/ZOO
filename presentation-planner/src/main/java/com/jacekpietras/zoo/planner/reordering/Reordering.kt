package com.jacekpietras.zoo.planner.reordering

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
    additionalOffset: Int?,
    key: Any,
    index: Int,
    lazyListState: LazyListState,
    onOrderingChange: (ReorderingData?) -> Unit,
    onDragStop: (from: Int, to: Int) -> Unit,
): Modifier = composed {
    if (isFixed) return@composed this

    val offsetY = remember { mutableStateOf(0f) }
    val overOffset = remember { mutableStateOf(0f) }
    val indexOfAdditionalOffset = remember { mutableStateOf<Int?>(null) }
    val shouldAnimateReturn = remember { mutableStateOf(false) }
    val shouldAnimateReturnIndex = remember { mutableStateOf<Int?>(null) }
    val animateAdditionalOffset by animateIntAsState(targetValue = additionalOffset ?: 0)
    val animateOffset by animateFloatAsState(targetValue = offsetY.value)
    val animateOverOffset by animateFloatAsState(targetValue = overOffset.value)

    if (additionalOffset != null && additionalOffset != 0) {
        indexOfAdditionalOffset.value = index
    }
    if (shouldAnimateReturn.value) {
        shouldAnimateReturnIndex.value = index
    }

    val animateOffsetCombined = (animateOffset.takeIf { shouldAnimateReturnIndex.value == index } ?: animateOverOffset)
    val animateAdditionalOffsetCombined = (animateAdditionalOffset.takeIf { indexOfAdditionalOffset.value == index } ?: 0)

    pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDrag = { change, offset ->
                change.consume()
                offsetY.value += offset.y
                val data = getReorderingData(lazyListState, key = key, offsetY.value)
                overOffset.value = data.overOffset.toFloat()

                onOrderingChange(data)
            },
            onDragStart = {
                onOrderingChange(getReorderingData(lazyListState, key = key, 0f))
                shouldAnimateReturn.value = true
            },
            onDragEnd = {
                val reorderingData = getReorderingData(lazyListState, key = key, offsetY.value)
                onDragStop(reorderingData.fromIndex, reorderingData.toIndex)

                if (reorderingData.fromIndex != reorderingData.toIndex) {
                    shouldAnimateReturn.value = false
                }

                offsetY.value = 0f
                overOffset.value = 0f
                onOrderingChange(null)
            },
            onDragCancel = {
                offsetY.value = 0f
                overOffset.value = 0f
                onOrderingChange(null)
            }
        )
    }
        .graphicsLayer(translationY = animateOffsetCombined + animateAdditionalOffsetCombined)
        .zIndex(if (animateOffsetCombined != 0f) 1.0f else 0.0f)
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
                visibleItemsInfo.last().index
            } else {
                visibleItemsInfo.last().index + 1
            }

        val toIndex = if (keyIndex < firstAfter) {
            firstAfter - 1
        } else {
            firstAfter
        }.coerceIn(0..visibleItemsInfo.last().index)

        val firstAfterOffset = visibleItemsInfo.firstOrNull { it.index == toIndex }?.offset ?: 0
        val overOffset = keyOffset - firstAfterOffset + offset.toInt()

        ReorderingData(
            toIndex = toIndex,
            fromIndex = keyIndex,
            draggedHeight = height,
            overOffset = overOffset,
        )
    }

internal fun ReorderingData.getAdditionalOffset(index: Int, isFixed: Boolean): Int {
    if (isFixed) return 0

    val positiveOffset = if (index in toIndex until fromIndex) draggedHeight else 0
    val negativeOffset = if (index in (fromIndex + 1)..toIndex) -draggedHeight else 0

    return positiveOffset + negativeOffset
}

internal class ReorderingData(
    val fromIndex: Int = 0,
    val toIndex: Int = 0,
    val draggedHeight: Int = 0,
    val overOffset: Int = 0,
)
