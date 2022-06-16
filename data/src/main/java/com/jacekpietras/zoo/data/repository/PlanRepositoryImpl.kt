package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.data.database.dao.PlanDao
import com.jacekpietras.zoo.data.database.mapper.PlanMapper
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

internal class PlanRepositoryImpl(
    private val planDao: PlanDao,
    private val planMapper: PlanMapper,
) : PlanRepository {

    override suspend fun setPlan(plan: PlanEntity) {
        plan
            .let(planMapper::from)
            .let { planDao.insert(it) }
    }

    override fun observePlan(planId: PlanId): Flow<PlanEntity> =
        planDao.observePlan(planId.id)
            .filterNotNull()
            .map(planMapper::from)

    override suspend fun getPlan(planId: PlanId): PlanEntity? =
        planDao.getPlan(planId.id)
            ?.let(planMapper::from)
}
