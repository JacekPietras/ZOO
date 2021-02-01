package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class MapRepositoryImpl : MapRepository {

    override suspend fun getMapDataUseCase(): Flow<List<MapItemEntity>> =
        listOf(
            listOf(
                PathEntity(20f to 20f, 25f to 25f, 25f to 30f),
                PathEntity(21f to 20f, 26f to 25f, 26f to 30f, 20f to 20f),
                PolygonEntity(19f to 22f, 24f to 25f, 24f to 30f, 18f to 22f),
            )
        ).asFlow()
}