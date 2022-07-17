package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.core.text.RichText

internal sealed class MapEffect {

    data class ShowToast(
        val text: RichText,
    ) : MapEffect()
}