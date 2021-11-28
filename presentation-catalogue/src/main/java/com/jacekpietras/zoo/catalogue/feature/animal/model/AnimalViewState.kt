package com.jacekpietras.zoo.catalogue.feature.animal.model

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
)
