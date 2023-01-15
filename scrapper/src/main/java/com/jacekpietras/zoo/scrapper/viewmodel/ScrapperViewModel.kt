package com.jacekpietras.zoo.scrapper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.scrapper.data.WebScrapper
import com.jacekpietras.zoo.scrapper.model.ScrapperState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class ScrapperViewModel(
    private val webScrapper: WebScrapper,
) : ViewModel() {
    private val state = MutableStateFlow(ScrapperState())
    val viewState: Flow<ScrapperState> = state

    init {
        viewModelScope.launch {
            val notKnownAnimals = webScrapper.scrapAllAnimals()
            state.value = state.value.copy(notKnownAnimals = notKnownAnimals)
        }
    }
}