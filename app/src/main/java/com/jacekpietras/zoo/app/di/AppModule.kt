package com.jacekpietras.zoo.app.di

import com.jacekpietras.zoo.app.contract.tracking.interactor.*
import com.jacekpietras.zoo.tracking.contract.interactor.*
import org.koin.dsl.module

val appModule = module {

    factory<OnLocationUpdateUseCase> {
        OnLocationUpdateUseCaseAdapter(
            insertUserPositionUseCase = get(),
        )
    }

    factory<OnCompassUpdateUseCase> {
        OnCompassUpdateUseCaseAdapter(
            insertUserCompassUseCase = get(),
        )
    }

    factory<OnLightSensorUpdateUseCase> {
        OnLightSensorUpdateUseCaseAdapter(
            gpsRepository = get(),
        )
    }

    factory<ObserveCompassEnabledUseCase> {
        ObserveCompassEnabledUseCaseAdapter(
            gpsRepository = get(),
        )
    }

    factory<ObserveLightSensorEnabledUseCase> {
        ObserveLightSensorEnabledUseCaseImpl(
            gpsRepository = get(),
        )
    }

    factory<ObserveNavigationEnabledUseCase> {
        ObserveNavigationEnabledUseCaseAdapter(
            gpsRepository = get(),
        )
    }
}
