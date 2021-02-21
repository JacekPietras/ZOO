package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.zoo.domain.model.AnimalEntity

interface AnimalRepository {

    suspend fun getMyszojelen(): AnimalEntity
}