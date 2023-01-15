package com.jacekpietras.zoo.scrapper.di

import com.jacekpietras.zoo.scrapper.data.WebScrapper
import com.jacekpietras.zoo.scrapper.viewmodel.ScrapperViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val scrapperModule = module {

    single {
        WebScrapper(
            moshi = get(),
            animalRepository = get(),
        )
    }

    viewModel {
        ScrapperViewModel(
            webScrapper = get(),
        )
    }
}
