package com.jacekpietras.zoo.domain.feature.planner.repository

import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanId
import kotlinx.coroutines.flow.Flow

interface PlanRepository {

    suspend fun setPlan(plan: PlanEntity)

    fun observePlan(planId: PlanId): Flow<PlanEntity>

    suspend fun getPlan(planId: PlanId): PlanEntity?
}
