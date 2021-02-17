package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.domain.interactor.InsertUserCompassUseCase
import com.jacekpietras.zoo.tracking.OnCompassUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnCompassUpdateImpl(
    private val insertUserCompassUseCase: InsertUserCompassUseCase
) : OnCompassUpdate {

    override fun invoke(angle: Float) {
        CoroutineScope(Dispatchers.IO).launch {
            insertUserCompassUseCase(angle)
        }
    }
}