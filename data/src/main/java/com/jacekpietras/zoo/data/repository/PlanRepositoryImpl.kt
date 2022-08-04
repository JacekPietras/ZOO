package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.data.database.dao.PlanDao
import com.jacekpietras.zoo.data.database.mapper.PlanMapper
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

internal class PlanRepositoryImpl(
    private val planDao: PlanDao,
    private val planMapper: PlanMapper,
    private val currentPlan: MutableStateFlow<PlanEntity?>,
) : PlanRepository {

    override suspend fun setPlan(plan: PlanEntity) {
        if (plan.planId == PlanEntity.CURRENT_PLAN_ID) {
            currentPlan.value = plan
        }
        plan
            .let(planMapper::from)
            .let { planDao.insert(it) }
    }

    override fun observePlan(planId: PlanId): Flow<PlanEntity> =
        if (planId == PlanEntity.CURRENT_PLAN_ID) {
            currentPlan
                .onStart { emit(getPlan(planId)) }
                .filterNotNull()
        } else {
            planDao.observePlan(planId.id)
                .filterNotNull()
                .map(planMapper::from)
        }

    override suspend fun getPlan(planId: PlanId): PlanEntity? =
        planDao.getPlan(planId.id)
            ?.let(planMapper::from)
}
