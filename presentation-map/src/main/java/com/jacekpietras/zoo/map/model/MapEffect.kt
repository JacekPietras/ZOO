package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.core.text.Text

internal sealed class MapEffect {

    data class ShowToast(
        val text: Text,
    ) : MapEffect()

    object CenterAtUser : MapEffect()

    data class CenterAtPoint(
        val point: PointD,
    ) : MapEffect()
}