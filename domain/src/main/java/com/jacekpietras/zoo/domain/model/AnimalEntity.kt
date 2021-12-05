package com.jacekpietras.zoo.domain.model

import com.jacekpietras.zoo.domain.model.Division.AMPHIBIAN
import com.jacekpietras.zoo.domain.model.Division.BIRD
import com.jacekpietras.zoo.domain.model.Division.FISH
import com.jacekpietras.zoo.domain.model.Division.MAMMAL
import com.jacekpietras.zoo.domain.model.Division.REPTILE

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

class AnimalId(id: String) {

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
}

enum class Division {
    MAMMAL,
    BIRD,
    AMPHIBIAN,
    REPTILE,
    FISH,
    ARTHROPOD,
    MOLLUSCA,
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

