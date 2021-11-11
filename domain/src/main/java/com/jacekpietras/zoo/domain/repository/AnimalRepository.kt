package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AnimalRepository {

    suspend fun scrapAllAnimals()

    suspend fun scrapTestAnimals()

    fun getAnimals(regionId: RegionId): List<AnimalEntity>

    fun getAnimals(division: Division? = null): List<AnimalEntity>

    fun observeAnimals(filter: AnimalFilter): Flow<List<AnimalEntity>>

    fun getAnimal(animalId: AnimalId): AnimalEntity
}