package com.jacekpietras.zoo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacekpietras.zoo.data.database.model.FavoriteDto
import com.jacekpietras.zoo.data.database.model.PlanDto
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PlanDao {

    @Query("SELECT * FROM `plan` WHERE planId = :planId")
    fun observePlan(planId:String): Flow<PlanDto?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlanDto)
}