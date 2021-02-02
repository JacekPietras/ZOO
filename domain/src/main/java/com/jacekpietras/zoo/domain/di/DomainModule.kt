package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.GetMapDataUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPosition
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetMapDataUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetUserPosition()
    }
}