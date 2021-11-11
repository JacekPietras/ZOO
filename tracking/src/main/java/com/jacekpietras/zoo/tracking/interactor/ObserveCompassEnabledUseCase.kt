package com.jacekpietras.zoo.tracking.interactor

import kotlinx.coroutines.flow.Flow

interface ObserveCompassEnabledUseCase {

    fun run(): Flow<Boolean>
}