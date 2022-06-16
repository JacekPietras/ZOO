package com.jacekpietras.zoo.app.di

import com.jacekpietras.zoo.app.*
import com.jacekpietras.zoo.tracking.interactor.*
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

    factory<OnLightSensorUpdate> {
        OnLightSensorUpdateImpl(
            gpsRepository = get(),
        )
    }

    factory<ObserveCompassEnabledUseCase> {
        ObserveCompassEnabledUseCaseImpl(
            gpsRepository = get(),
        )
    }

    factory<ObserveLightSensorEnabledUseCase> {
        ObserveLightSensorEnabledUseCaseImpl(
            gpsRepository = get(),
        )
    }

    factory<ObserveNavigationEnabledUseCase> {
        ObserveNavigationEnabledUseCaseImpl(
            gpsRepository = get(),
        )
    }
}
