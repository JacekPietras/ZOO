package com.jacekpietras.zoo.map.model

internal sealed class MapEffect {

    data class ShowToast(
        val text: String,
    ) : MapEffect()

    object CenterAtUser : MapEffect()
}