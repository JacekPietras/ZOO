package com.jacekpietras.zoo.domain.feature.sensors.di

import com.jacekpietras.zoo.domain.feature.sensors.interactor.*
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
        )
    }
    factory {
        ObserveUserPositionUseCase(
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
