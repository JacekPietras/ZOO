package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.FindRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsByDivisionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAviaryUseCase
import com.jacekpietras.zoo.domain.interactor.GetBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.GetLinesUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionCenterPointUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsContainingPointUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsWithAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.interactor.GetShortestPathUseCase
import com.jacekpietras.zoo.domain.interactor.GetSnapPathToRoadUseCase
import com.jacekpietras.zoo.domain.interactor.GetTechnicalRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.GetTerminalNodesUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetVisitedRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.GetWorldBoundsUseCase
import com.jacekpietras.zoo.domain.interactor.InsertUserCompassUseCase
import com.jacekpietras.zoo.domain.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveCompassUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveFilteredAnimalsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveOldTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveWorldBoundsUseCase
import com.jacekpietras.zoo.domain.interactor.StartCompassUseCase
import com.jacekpietras.zoo.domain.interactor.StopCompassUseCase
import com.jacekpietras.zoo.domain.interactor.UploadHistoryUseCase
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetRegionsInUserPositionUseCase(
            getRegionsContainingPointUseCase = get(),
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
        FindRegionUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetRegionsContainingPointUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetRegionCenterPointUseCase(
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
        ObserveFilteredAnimalsUseCase(
            animalRepository = get()
        )
    }
    factory {
        GetAnimalUseCase(
            animalRepository = get()
        )
    }
    factory {
        GetAnimalsByDivisionUseCase(
            animalRepository = get()
        )
    }
    factory {
        GetAnimalsInUserPositionUseCase(
            getAnimalsInRegionUseCase = get(),
            getRegionsInUserPositionUseCase = get(),
        )
    }
    factory {
        GetRegionsWithAnimalsInUserPositionUseCase(
            getRegionsContainingPointUseCase = get(),
            getAnimalsInRegionUseCase = get(),
            gpsRepository = get(),
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
        GetShortestPathUseCase()
    }
    factory {
        GetShortestPathFromUserUseCase(
            getUserPositionUseCase = get(),
        )
    }
    factory {
        GetTechnicalRoadsUseCase(
            mapRepository = get(),
        )
    }
    factory {
        GetVisitedRoadsUseCase(
            mapRepository = get(),
            gpsRepository = get(),
            getSnapPathToRoadUseCase = get(),
        )
    }
    factory {
        GetLinesUseCase(
            mapRepository = get(),
        )
    }
    factory {
        LoadAnimalsUseCase(
            animalRepository = get(),
        )
    }
    factory {
        ObserveTakenRouteUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        ObserveOldTakenRouteUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        GetSnapPathToRoadUseCase(
//            getSnappedToRoadUseCase = get(),
//            getShortestPathUseCase = get()
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