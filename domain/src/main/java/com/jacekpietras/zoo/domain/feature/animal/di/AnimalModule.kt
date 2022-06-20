package com.jacekpietras.zoo.domain.feature.animal.di

import com.jacekpietras.zoo.domain.feature.animal.interactor.*
import org.koin.dsl.module

val animalModule = module {
    factory {
        GetAnimalUseCase(
            animalRepository = get()
        )
    }
    factory {
        ObserveFilteredAnimalsUseCase(
            animalRepository = get()
        )
    }
    factory {
        GetAnimalsByDivisionUseCase(
            animalRepository = get()
        )
    }
    factory {
        LoadAnimalsUseCase(
            animalRepository = get(),
        )
    }
    factory {
        IsAnimalSeenUseCase(
            getAnimalUseCase = get(),
            isRegionSeenUseCase = get(),
        )
    }
    factory {
        GetAnimalPositionUseCase(
            getAnimalUseCase = get(),
            getRegionCenterPointUseCase = get(),
        )
    }
}
