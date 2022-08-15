package com.jacekpietras.zoo.catalogue.feature.animal.model

import com.jacekpietras.zoo.core.text.RichText

internal sealed class AnimalEffect {

    data class ShowToast(
        val text: RichText,
    ) : AnimalEffect()
}