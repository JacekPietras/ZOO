package com.jacekpietras.zoo.domain.feature.planner.interactor

import com.jacekpietras.zoo.domain.feature.favorites.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity.Companion.CURRENT_PLAN_ID
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCurrentPlanUseCase(
    private val planRepository: PlanRepository,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val isAnimalInPlanUseCase: IsAnimalFavoriteUseCase,
) {

    fun run(): Flow<PlanEntity> =
        planRepository.observePlan(CURRENT_PLAN_ID)
            .map { plan ->
                plan.copy(
                    stages = plan.stages.map { stage ->
                        stage.copy(
                            animals = getAnimalsInRegionUseCase.run(stage.regionId)
                                .filter { isAnimalInPlanUseCase.run(it.id) }
                        )
                    }
                )
            }
}
