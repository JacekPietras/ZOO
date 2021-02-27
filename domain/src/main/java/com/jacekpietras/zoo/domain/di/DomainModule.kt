package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.*
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetRegionsInUserPositionUseCase(
            getRegionsContainingPointUseCase = get(),
            gpsRepository = get(),
        )
    }
    factory {
        GetRegionsContainingPointUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetBuildingsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetAnimalsInRegionUseCase(
            animalRepository = get()
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
        GetSnappedToRoadUseCase()
    }
    factory {
        GetShortestPathUseCase(
            getUserPositionUseCase = get(),
        )
    }
    factory {
        GetTechnicalRoadsUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetLinesUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetMyszojelenUseCase(
            animalRepository = get(),
        )
    }
    factory {
        ObserveTakenRouteUseCase(
            gpsRepository = get(),
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
    factory {
        GetTerminalNodesUseCase(
            getRoadsUseCase = get(),
            getTechnicalRoadsUseCase = get(),
        )
    }
}