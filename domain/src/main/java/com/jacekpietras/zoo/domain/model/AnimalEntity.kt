package com.jacekpietras.zoo.domain.model

data class AnimalEntity(
    val id: String = "id",
    val language: String = "pl",
    val name: String = "name",
    val nameLatin: String = "name",
    val occurrence: String = "occurrence",
    val environment: String = "environment",
    val food: String = "food",
    val multiplication: String = "multiplication",
    val protectionAndThreats: String = "protection and threats",
    val facts: String = "facts",
    val wiki: String = "",
    val photos: List<String> = emptyList(),
)