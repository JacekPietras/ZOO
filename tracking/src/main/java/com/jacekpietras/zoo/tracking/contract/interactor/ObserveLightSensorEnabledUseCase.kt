package com.jacekpietras.zoo.tracking.contract.interactor

import kotlinx.coroutines.flow.Flow

interface ObserveLightSensorEnabledUseCase {

    fun run(): Flow<Boolean>
}
