package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class MapViewState(
    val currentRegionIds: Flow<String> = MutableStateFlow(""),
    val worldBounds: Flow<RectD> = MutableStateFlow(RectD()),
    val mapData: Flow<List<MapItem>> = MutableStateFlow(emptyList()),
    val userPosition: Flow<PointD> = MutableStateFlow(PointD()),
    val effect: Channel<MapEffect> = Channel()
)

sealed class MapEffect {

    data class ShowToast(
        val text: String,
    ) : MapEffect()

    object CenterAtUser : MapEffect()
}
