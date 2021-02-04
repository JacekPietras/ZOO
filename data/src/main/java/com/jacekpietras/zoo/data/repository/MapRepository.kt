package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MapRepositoryImpl : MapRepository {

    override fun getBuildingsUseCase(): Flow<List<PolygonEntity>> =
        flowOf(
            listOf(
                PolygonEntity(19f to 22f, 24f to 25f, 24f to 30f, 18f to 22f),
            )
        )

    override fun getRoadsUseCase(): Flow<List<PathEntity>> =
        flowOf(
            listOf(
                PathEntity(20f to 20f, 25f to 25f, 25f to 30f),
                PathEntity(21f to 20f, 26f to 25f, 26f to 30f, 20f to 20f),
            )
        )
}