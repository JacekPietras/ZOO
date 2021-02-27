package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Division

interface AnimalRepository {

    suspend fun scrapAllAnimals()

    suspend fun scrapTestAnimals()

    fun getAnimalsInRegion(regionId: String): List<AnimalEntity>

    fun getAnimalsByDivision(division: Division? = null): List<AnimalEntity>
}