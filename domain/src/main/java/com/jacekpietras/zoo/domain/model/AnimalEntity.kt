package com.jacekpietras.zoo.domain.model

import com.jacekpietras.zoo.domain.model.Division.*
import java.util.*

data class AnimalEntity(
    val id: AnimalId = AnimalId("id"),
    val language: String = "pl",
    val name: String,
    val nameLatin: String,
    val occurrence: String,
    val environment: String,
    val food: String,
    val division: Division,
    val multiplication: String,
    val protectionAndThreats: String,
    val facts: String,
    val wiki: String = "todo",
    val web: String,
    val regionInZoo: String = "todo",
    val photos: List<String>,
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
        .toLowerCase(Locale.US)
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

