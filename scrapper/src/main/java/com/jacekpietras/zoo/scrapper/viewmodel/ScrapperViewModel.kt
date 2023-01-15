package com.jacekpietras.zoo.scrapper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.scrapper.data.WebScrapper
import kotlinx.coroutines.launch

internal class ScrapperViewModel(
    private val webScrapper: WebScrapper,
) : ViewModel() {

    fun scrap() {
        viewModelScope.launch {
            webScrapper.scrapAllAnimals()
        }
    }
}