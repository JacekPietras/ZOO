package com.jacekpietras.zoo.map.model

import androidx.compose.runtime.Immutable
import com.jacekpietras.zoo.core.text.RichText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class MapViewState(
    val isBackArrowShown: Boolean = false,
    val isGuidanceShown: Boolean = false,
    val title: RichText = RichText.Empty,
    val luminanceText: String = "",
    val navigationText: RichText,
    val isNavigationVisible: Boolean,
    val mapCarouselItems: ImmutableList<MapCarouselItem> = persistentListOf(),
    val isMapActionsVisible: Boolean = false,
    val mapActions: ImmutableList<MapAction> = persistentListOf(),
)
