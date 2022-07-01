package com.jacekpietras.zoo.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.map.model.MapAction

@Composable
internal fun MapActionChips(
    isVisible: Boolean,
    mapActions: List<MapAction>,
    onMapActionClicked: (MapAction) -> Unit,
) {
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(isVisible) }
            .apply { targetState = isVisible },
        modifier = Modifier.fillMaxWidth(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            items(mapActions) { mapAction ->
                Card(
                    shape = RoundedCornerShape(50),
                    elevation = 8.dp,
                ) {
                    Box(
                        modifier = Modifier.clickable {
                            onMapActionClicked(mapAction)
                        }
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            text = stringResource(mapAction.title),
                            color = MaterialTheme.colors.onSurface,
                        )
                    }
                }
            }
        }
    }
}
