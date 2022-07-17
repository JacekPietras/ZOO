package com.jacekpietras.zoo.app.di

import com.jacekpietras.zoo.app.contract.tracking.interactor.ObserveCompassEnabledUseCaseAdapter
import com.jacekpietras.zoo.app.contract.tracking.interactor.ObserveLightSensorEnabledUseCaseImpl
import com.jacekpietras.zoo.app.contract.tracking.interactor.ObserveNavigationEnabledUseCaseAdapter
import com.jacekpietras.zoo.app.contract.tracking.interactor.OnCompassUpdateUseCaseAdapter
import com.jacekpietras.zoo.app.contract.tracking.interactor.OnLightSensorUpdateUseCaseAdapter
import com.jacekpietras.zoo.app.contract.tracking.interactor.OnLocationUpdateUseCaseAdapter
import com.jacekpietras.zoo.app.viewmodel.MainViewModel
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveCompassEnabledUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveLightSensorEnabledUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveNavigationEnabledUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnCompassUpdateUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnLightSensorUpdateUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnLocationUpdateUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
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

    viewModel {
        MainViewModel(
            observeSuggestedThemeTypeUseCase = get(),
        )
    }
}
