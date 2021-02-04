package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.GetBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.GetRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPosition
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetBuildingsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetRoadsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetUserPosition()
    }
}