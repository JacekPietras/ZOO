package com.jacekpietras.zoo.map.model

import androidx.compose.runtime.Immutable
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.zoo.core.text.RichText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class MapVolatileViewState(
    val compass: Float = 0f,
    val userPosition: PointD = PointD(0.0, 0.0),
    val title: RichText = RichText.Empty,
    val content: RichText = RichText.Empty,
    val snappedPoint: PointD? = null,
    val mapData: ImmutableList<MapItem> = persistentListOf(),
)
