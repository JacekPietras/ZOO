package com.jacekpietras.zoo.tracking.interactor

import kotlinx.coroutines.flow.Flow

interface ObserveLightSensorEnabledUseCase {

    fun run(): Flow<Boolean>
}
