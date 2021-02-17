package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.*
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetRegionsInUserPositionUseCase(
            mapRepository = get(),
            gpsRepository = get(),
        )
    }
    factory {
        GetBuildingsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetAviaryUseCase(
            mapRepository = get()
        )
    }
    factory {
        ObserveWorldBoundsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetWorldBoundsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetRoadsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetTechnicalRoadsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetLinesUseCase(
            mapRepository = get()
        )
    }
    factory {
        ObserveTakenRouteUseCase(
            gpsRepository = get()
        )
    }
    factory {
        GetUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        InsertUserPositionUseCase(
            gpsRepository = get(),
            worldBoundsUseCase = get(),
        )
    }
    factory {
        UploadHistoryUseCase(
            mailGateway = get(),
            gpsRepository = get(),
        )
    }
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
}