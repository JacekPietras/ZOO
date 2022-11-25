package com.jacekpietras.zoo.domain.feature.animal.model

import com.jacekpietras.zoo.domain.feature.animal.model.Division.*
import com.jacekpietras.zoo.domain.model.RegionId

data class AnimalEntity(
    val id: AnimalId = AnimalId("id"),
    val language: String = "pl",
    val name: String,
    val nameLatin: String,
    val occurrence: String,
    val environment: String,
    val food: String,
    val feeding: List<Feeding> = emptyList(),
    val division: Division,
    val multiplication: String,
    val protectionAndThreats: String,
    val facts: String,
    val wiki: String = "todo",
    val web: String,
    val regionInZoo: List<RegionId> = emptyList(),
    val photos: List<String>,
)

data class Feeding(
    val time: String,
    val weekdays: List<Int>?,
    val note: String?,
)

class AnimalId(id: String) : Comparable<AnimalId> {

    val id: String = id
        .trim()
        .replace("ż", "z")
        .replace("ą", "a")
        .replace("ę", "e")
        .replace("ć", "c")
        .replace("ó", "o")
        .replace("ł", "l")
        .replace("ś", "s")
        .replace("ź", "z")
        .replace("ń", "n")
        .replace("Ż", "z")
        .replace("Ą", "a")
        .replace("Ę", "e")
        .replace("Ć", "c")
        .replace("Ó", "o")
        .replace("Ł", "l")
        .replace("Ś", "s")
        .replace("Ź", "z")
        .replace("Ń", "n")
        .replace(" ", "-")
        .lowercase()

    override fun compareTo(other: AnimalId): Int =
        id.compareTo(other.id)

    override fun hashCode(): Int =
        id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimalId

        if (id != other.id) return false

        return true
    }
}

enum class Division {
    MAMMAL,
    BIRD,
    AMPHIBIAN,
    REPTILE,
    FISH,
    ARTHROPOD,
    MOLLUSK,
}

fun divisionFromPolishTag(tag: String): Division =
    when (tag) {
        "ssaki" -> MAMMAL
        "ptaki" -> BIRD
        "gady" -> REPTILE
        "plazy" -> AMPHIBIAN
        "ryby" -> FISH
        else -> throw IllegalArgumentException("unknown animal division $tag")
    }

