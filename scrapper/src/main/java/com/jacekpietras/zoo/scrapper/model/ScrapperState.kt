package com.jacekpietras.zoo.scrapper.model

internal data class ScrapperState(
    val notKnownAnimals: List<String> = emptyList(),
)