package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.zoo.core.text.Text

internal data class AnimalViewState(
    val title: Text = Text.Empty,
    val subTitle: Text = Text.Empty,
    val content: Text = Text.Empty,
    val isWikiLink: Boolean = false,
    val isWebLink: Boolean = false,
    val isNavLink: Boolean = false,
    val images: List<String> = emptyList(),
)
