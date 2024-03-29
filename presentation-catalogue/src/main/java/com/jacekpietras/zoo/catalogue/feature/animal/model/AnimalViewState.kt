package com.jacekpietras.zoo.catalogue.feature.animal.model

import androidx.compose.runtime.Immutable
import com.jacekpietras.geometry.RectD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.zoo.core.text.RichText

@Immutable
internal data class AnimalViewState(
    val title: RichText,
    val subTitle: RichText,
    val content: List<TextParagraph>,
    val feeding: RichText?,
    val isWikiLinkVisible: Boolean,
    val isWebLinkVisible: Boolean,
    val isNavLinkVisible: Boolean,
    val isSeen: Boolean?,
    val favoriteButtonText: RichText,
    val images: List<String>,
    val worldBounds: RectD = RectD(0.0, 0.0, 0.0, 0.0),
    val mapData: List<MapItem> = emptyList(),
)
