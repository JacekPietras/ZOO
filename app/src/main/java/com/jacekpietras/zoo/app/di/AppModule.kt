package com.jacekpietras.zoo.app.di

import com.jacekpietras.zoo.app.ObserveCompassEnabledUseCaseImpl
import com.jacekpietras.zoo.app.OnCompassUpdateImpl
import com.jacekpietras.zoo.app.OnLocationUpdateImpl
import com.jacekpietras.zoo.tracking.interactor.ObserveCompassEnabledUseCase
import com.jacekpietras.zoo.tracking.interactor.OnCompassUpdate
import com.jacekpietras.zoo.tracking.interactor.OnLocationUpdate
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

    factory<ObserveCompassEnabledUseCase> {
        ObserveCompassEnabledUseCaseImpl(
            gpsRepository = get(),
        )
    }
}
