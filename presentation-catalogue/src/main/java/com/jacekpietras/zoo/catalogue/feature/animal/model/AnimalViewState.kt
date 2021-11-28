package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.zoo.core.text.Text

internal data class AnimalViewState(
    val title: Text,
    val subTitle: Text,
    val content: List<TextParagraph>,
    val isWikiLinkVisible: Boolean,
    val isWebLinkVisible: Boolean,
    val isNavLinkVisible: Boolean,
    val isSeen: Boolean?,
    val favoriteButtonText: Text,
    val images: List<String>,
    val worldBounds: RectD = RectD(0.0, 0.0, 0.0, 0.0),
    val mapData: List<MapItem> = emptyList(),
)
