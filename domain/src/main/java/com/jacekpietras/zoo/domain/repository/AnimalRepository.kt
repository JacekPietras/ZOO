package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.AnimalEntity

interface AnimalRepository {

    suspend fun scrapAllAnimals()

    suspend fun scrapTestAnimals()

    fun getAnimalsInRegion(regionId: String): List<AnimalEntity>
}