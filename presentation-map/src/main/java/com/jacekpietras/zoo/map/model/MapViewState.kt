package com.jacekpietras.zoo.map.model

import androidx.compose.runtime.Immutable
import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.core.text.RichText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class MapViewState(
    val isNightThemeSuggested: Boolean = false,
    val isBackArrowShown: Boolean = false,
    val isGuidanceShown: Boolean = false,
    val title: RichText = RichText.Empty,
    val mapCarouselItems: ImmutableList<MapCarouselItem> = persistentListOf(),
    val isMapActionsVisible: Boolean = false,
    val mapActions: ImmutableList<MapAction> = persistentListOf(),
)
