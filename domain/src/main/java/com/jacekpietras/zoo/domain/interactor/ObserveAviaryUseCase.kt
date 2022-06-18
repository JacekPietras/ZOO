package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ObserveAviaryUseCase(
    private val mapRepository: MapRepository,
    private val planRepository: PlanRepository,
) {

    suspend fun run(): Flow<List<PolygonEntity>> =
        mapRepository.observeAviary().map { withContext(Dispatchers.Default) { gratisPackage() } }

    //fixme remove it
    private suspend fun gratisPackage(): List<PolygonEntity> =
        planRepository.getPlan(PlanEntity.CURRENT_PLAN_ID)?.stages?.filterIsInstance<Stage.InRegion>()?.map {
            mapRepository.getCurrentRegions().first { a -> a.first.id == it.regionId }.second
        } ?: emptyList()
}