package com.jacekpietras.zoo.domain.feature.pathfinder.interactor

import com.jacekpietras.geometry.PointD

interface GetShortestPathUseCase{

    suspend fun run(start: PointD, end: PointD): List<PointD>
}

