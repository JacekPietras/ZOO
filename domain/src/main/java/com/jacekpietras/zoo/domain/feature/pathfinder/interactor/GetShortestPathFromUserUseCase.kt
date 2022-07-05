package com.jacekpietras.zoo.domain.feature.pathfinder.interactor

import com.jacekpietras.geometry.PointD

interface GetShortestPathFromUserUseCase {

    suspend fun run(point: PointD): List<PointD>
}
