package com.jacekpietras.zoo.domain.feature.animal.di

import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalPositionUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.IsAnimalSeenUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.ObserveFilteredAnimalsUseCase
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
