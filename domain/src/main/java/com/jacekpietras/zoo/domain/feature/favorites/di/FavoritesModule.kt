package com.jacekpietras.zoo.domain.feature.favorites.di

import com.jacekpietras.zoo.domain.feature.favorites.interactor.ObserveAnimalFavoritesUseCase
import com.jacekpietras.zoo.domain.feature.favorites.interactor.IsAnimalFavoriteUseCase
import com.jacekpietras.zoo.domain.feature.favorites.interactor.SetAnimalFavoriteUseCase
import org.koin.dsl.module

val favoritesModule = module {
    factory {
        IsAnimalFavoriteUseCase(
            favoritesRepository = get(),
        )
    }
    factory {
        ObserveAnimalFavoritesUseCase(
            favoritesRepository = get(),
        )
    }
    factory {
        SetAnimalFavoriteUseCase(
            favoritesRepository = get(),
        )
    }
}