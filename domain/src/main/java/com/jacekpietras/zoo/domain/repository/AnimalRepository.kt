package com.jacekpietras.zoo.domain.repository

interface AnimalRepository {

    suspend fun scrapAllAnimals()

    suspend fun scrapTestAnimals()
}