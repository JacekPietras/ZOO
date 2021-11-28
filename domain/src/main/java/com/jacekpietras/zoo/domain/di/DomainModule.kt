package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.FindRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsByDivisionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionCenterPointUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsContainingPointUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.interactor.GetShortestPathUseCase
import com.jacekpietras.zoo.domain.interactor.GetTerminalNodesUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.GetWorldBoundsUseCase
import com.jacekpietras.zoo.domain.interactor.InitializeGraphAnalyzerIfNeededUseCase
import com.jacekpietras.zoo.domain.interactor.InsertUserCompassUseCase
import com.jacekpietras.zoo.domain.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.interactor.IsAnimalSeenUseCase
import com.jacekpietras.zoo.domain.interactor.IsRegionSeenUseCase
import com.jacekpietras.zoo.domain.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.interactor.LoadMapUseCase
import com.jacekpietras.zoo.domain.interactor.LoadVisitedRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveAviaryUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveCompassUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveFilteredAnimalsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveMapLinesUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveOldTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionsWithAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveTechnicalRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveVisitedRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveWorldBoundsUseCase
import com.jacekpietras.zoo.domain.interactor.SetAnimalFavoriteUseCase
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
        ObserveBuildingsUseCase(
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
        ObserveRegionsWithAnimalsInUserPositionUseCase(
            getRegionsContainingPointUseCase = get(),
            getAnimalsInRegionUseCase = get(),
            gpsRepository = get(),
        )
    }
    factory {
        ObserveAviaryUseCase(
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
        ObserveRoadsUseCase(
            mapRepository = get()
        )
    }
    factory {
        InitializeGraphAnalyzerIfNeededUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetShortestPathUseCase(
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
    factory {
        GetShortestPathFromUserUseCase(
            getUserPositionUseCase = get(),
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
    factory {
        ObserveTechnicalRoadsUseCase(
            mapRepository = get(),
        )
    }
    factory {
        ObserveVisitedRoadsUseCase(
            mapRepository = get(),
        )
    }
    factory {
        LoadMapUseCase(
            mapRepository = get(),
        )
    }
    factory {
        LoadVisitedRouteUseCase(
            mapRepository = get(),
            gpsRepository = get(),
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
    factory {
        ObserveMapLinesUseCase(
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
        GetUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        InsertUserPositionUseCase(
            gpsRepository = get(),
            mapRepository = get(),
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
            initializeGraphAnalyzerIfNeededUseCase = get(),
        )
    }
    factory {
        IsAnimalSeenUseCase(
            getAnimalUseCase = get(),
            isRegionSeenUseCase = get(),
        )
    }
    factory {
        IsRegionSeenUseCase(
            mapRepository = get(),
        )
    }
    factory {
        IsAnimalFavoriteUseCase(
            favoritesRepository = get(),
        )
    }
    factory {
        SetAnimalFavoriteUseCase(
            favoritesRepository = get(),
        )
    }
}