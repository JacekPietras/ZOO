package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD

interface GetShortestPathFromUserUseCase {

    suspend fun run(point: PointD): List<PointD>
}
