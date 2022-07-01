package com.jacekpietras.zoo.tracking.contract.interactor

import kotlinx.coroutines.flow.Flow

interface ObserveNavigationEnabledUseCase {

    fun run(): Flow<Boolean>
}