package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.geometry.PointD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ObserveTerminalNodesUseCase(
    private val getTerminalNodesUseCase: GetTerminalNodesUseCase
) {

    fun run(): Flow<List<PointD>> = flow {
        emit(getTerminalNodesUseCase.run())
    }
}
