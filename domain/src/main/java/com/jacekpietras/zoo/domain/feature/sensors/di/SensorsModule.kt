package com.jacekpietras.zoo.domain.feature.sensors.di

import com.jacekpietras.zoo.domain.feature.sensors.interactor.GetUserPositionUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.InsertUserCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.InsertUserPositionUseCaseImpl
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveArrivalAtRegionEventUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveOutsideWorldEventUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveUserPositionUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartLightSensorUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartNavigationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopLightSensorUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopNavigationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.UploadHistoryUseCase
import org.koin.dsl.module

val sensorsModule = module {
    factory {
        ObserveCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        InsertUserCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StopCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StartCompassUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StopLightSensorUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StartLightSensorUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StopNavigationUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        StartNavigationUseCase(
            gpsRepository = get(),
            trackingServiceGateway = get(),
        )
    }
    factory {
        ObserveUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        GetUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory<InsertUserPositionUseCase> {
        InsertUserPositionUseCaseImpl(
            gpsRepository = get(),
            mapRepository = get(),
            worldBoundsUseCase = get(),
            stopNavigationUseCase = get(),
            pathListSnapper = get(),
            pathSnapper = get(),
            gpsEventsRepository = get(),
            getOrCreateCurrentPlanUseCase = get(),
            getRegionUseCase = get(),
            planRepository = get(),
        )
    }
    factory {
        UploadHistoryUseCase(
            mailGateway = get(),
            gpsRepository = get(),
        )
    }
    factory {
        ObserveOutsideWorldEventUseCase(
            gpsEventsRepository = get(),
        )
    }
    factory {
        ObserveArrivalAtRegionEventUseCase(
            gpsEventsRepository = get(),
        )
    }
}
