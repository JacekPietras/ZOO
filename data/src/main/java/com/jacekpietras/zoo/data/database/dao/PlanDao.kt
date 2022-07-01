package com.jacekpietras.zoo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacekpietras.zoo.data.database.model.PlanDto
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PlanDao {

    @Query("SELECT * FROM `plan` WHERE planId = :planId")
    fun observePlan(planId: String): Flow<PlanDto?>

    @Query("SELECT * FROM `plan` WHERE planId = :planId")
    fun getPlan(planId: String): PlanDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(plan: PlanDto)
}