package com.jacekpietras.zoo.domain.feature.favorites.interactor

import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository
import com.jacekpietras.zoo.domain.model.AnimalId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SetAnimalFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    suspend fun run(animalId: AnimalId, isFavorite: Boolean) {
        favoritesRepository.setFavorite(animalId, isFavorite)
        launchInBg {

//            addAnimalToCurrentPlanUseCase.run(animalId)

//            val isSeen = isAnimalSeenUseCase.run(animalId)
//            if (!isSeen) {
//
//            }
        }
    }

    private suspend fun launchInBg(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope {
            launch(dispatcher) {
                block.invoke(this)
            }
        }
    }
}
