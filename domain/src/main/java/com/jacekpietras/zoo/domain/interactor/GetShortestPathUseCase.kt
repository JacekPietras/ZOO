package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD

interface GetShortestPathUseCase{

    suspend fun run(start: PointD, end: PointD): List<PointD>
}

