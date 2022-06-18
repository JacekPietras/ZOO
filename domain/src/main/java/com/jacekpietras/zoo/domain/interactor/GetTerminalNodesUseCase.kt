package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD

interface GetTerminalNodesUseCase {

    suspend fun run(): List<PointD>
}
