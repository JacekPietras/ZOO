package com.jacekpietras.zoo.tracking.contract.interactor

import kotlinx.coroutines.flow.Flow

interface ObserveCompassEnabledUseCase {

    fun run(): Flow<Boolean>
}