package com.jacekpietras.zoo.domain.feature.animal.repository

import com.jacekpietras.zoo.domain.feature.animal.model.AnimalFilter
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.flow.Flow

interface AnimalRepository {

    suspend fun loadAnimals()

    fun getAnimals(): List<AnimalEntity>

    fun getAnimals(regionId: RegionId): List<AnimalEntity>

    fun observeAnimals(filter: AnimalFilter): Flow<List<AnimalEntity>>

    fun getAnimal(animalId: AnimalId): AnimalEntity
}