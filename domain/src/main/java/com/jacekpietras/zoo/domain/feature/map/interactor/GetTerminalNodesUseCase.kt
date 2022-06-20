package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.core.PointD

interface GetTerminalNodesUseCase {

    suspend fun run(): List<PointD>
}
