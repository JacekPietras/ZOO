package com.jacekpietras.zoo.data.repository.di

import com.jacekpietras.zoo.data.repository.MapRepositoryImpl
import com.jacekpietras.zoo.domain.repository.MapRepository
import org.koin.dsl.module

internal val repositoryModule = module {

    factory<MapRepository> {
        MapRepositoryImpl()
    }
}