package com.jacekpietras.zoo.domain.feature.animal.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.interactor.GetRegionCenterPointUseCase
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId

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