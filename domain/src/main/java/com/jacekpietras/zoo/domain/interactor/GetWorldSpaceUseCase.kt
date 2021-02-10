package com.jacekpietras.zoo.domain.interactor

import android.graphics.RectF
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class GetWorldSpaceUseCase(
    private val mapRepository: MapRepository,
) {

    operator fun invoke(): Flow<RectF> =
        mapRepository.getWorldSpace()
}