package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.AnimalId

class GetAnimalPositionUseCase(
    private val getAnimalUseCase: GetAnimalUseCase,
    private val getRegionCenterPointUseCase: GetRegionCenterPointUseCase,
) {

    suspend fun run(animalId: AnimalId): List<PointD> {
        val animal = getAnimalUseCase.run(animalId = animalId)
        return animal.regionInZoo.map {
            getRegionCenterPointUseCase.run(it)
        }
    }
}