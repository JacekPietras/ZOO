package com.jacekpietras.zoo.domain.model

import com.jacekpietras.zoo.domain.model.Division.MAMMAL
import java.util.*

data class AnimalEntity(
    val id: AnimalId = AnimalId("id"),
    val language: String = "pl",
    val name: String = "name",
    val nameLatin: String = "name",
    val occurrence: String = "occurrence",
    val environment: String = "environment",
    val food: String = "food",
    val division: Division = MAMMAL,
    val multiplication: String = "multiplication",
    val protectionAndThreats: String = "protection and threats",
    val facts: String = "facts",
    val wiki: String = "www",
    val web: String = "www",
    val photos: List<String> = emptyList(),
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
}
