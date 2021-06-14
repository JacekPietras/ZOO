package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalFilter
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.Division
import kotlinx.coroutines.flow.Flow

interface AnimalRepository {

    suspend fun scrapAllAnimals()

    suspend fun scrapTestAnimals()

    fun getAnimals(regionId: String): List<AnimalEntity>

    fun getAnimals(division: Division? = null): List<AnimalEntity>

    fun observeAnimals(filter: AnimalFilter): Flow<List<AnimalEntity>>

    fun getAnimal(animalId: AnimalId): AnimalEntity
}