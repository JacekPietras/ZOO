package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.favorites.interactor.ObserveAnimalFavoritesUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveCurrentPlanStagesWithAnimalsAndOptimizationUseCase(
    private val observeCurrentPlanWithOptimizationUseCase: ObserveCurrentPlanWithOptimizationUseCase,
    private val observeAnimalFavoritesUseCase: ObserveAnimalFavoritesUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
) {

    fun run(): Flow<List<Pair<Stage, List<AnimalEntity>>>> =
        observeCurrentPlanWithOptimizationUseCase.run()
            .combine(observeAnimalFavoritesUseCase.run()) { (stages, _), favorites ->
                stages
                    .map { stage ->
                        stage to stage.getAnimals(favorites)
                    }
            }

    private suspend fun Stage.getAnimals(favorites: List<AnimalId>): List<AnimalEntity> =
        if (this is Stage.InRegion) {
            getAnimalsInRegionUseCase.run(region.id)
                .filter { animal -> favorites.contains(animal.id) }
        } else {
            emptyList()
        }
}
