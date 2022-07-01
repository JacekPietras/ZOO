package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.core.text.RichText

internal sealed class MapEffect {

    data class ShowToast(
        val text: RichText,
    ) : MapEffect()

    object CenterAtUser : MapEffect()

    data class CenterAtPoint(
        val point: PointD,
    ) : MapEffect()
}