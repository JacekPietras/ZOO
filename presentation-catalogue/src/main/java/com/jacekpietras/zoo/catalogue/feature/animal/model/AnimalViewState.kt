package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.zoo.core.text.RichText

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
