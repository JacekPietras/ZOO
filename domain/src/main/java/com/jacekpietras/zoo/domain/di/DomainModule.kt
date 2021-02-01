package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.GetMapDataUseCase
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetMapDataUseCase(
            mapRepository = get()
        )
    }
}