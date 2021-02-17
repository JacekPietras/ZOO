package com.jacekpietras.zoo.app.di

import com.jacekpietras.zoo.app.OnCompassUpdateImpl
import com.jacekpietras.zoo.app.OnLocationUpdateImpl
import com.jacekpietras.zoo.tracking.OnCompassUpdate
import com.jacekpietras.zoo.tracking.OnLocationUpdate
import org.koin.dsl.module

val appModule = module {

    factory<OnLocationUpdate> {
        OnLocationUpdateImpl(
            insertUserPositionUseCase = get(),
        )
    }

    factory<OnCompassUpdate> {
        OnCompassUpdateImpl(
            insertUserCompassUseCase = get(),
        )
    }
}
