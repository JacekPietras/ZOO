package com.jacekpietras.zoo.tracking.interactor

import kotlinx.coroutines.flow.Flow

interface ObserveNavigationEnabledUseCase {

    fun run(): Flow<Boolean>
}