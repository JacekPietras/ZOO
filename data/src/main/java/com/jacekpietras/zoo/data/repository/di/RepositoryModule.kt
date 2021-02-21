package com.jacekpietras.zoo.data.repository.di

import com.jacekpietras.zoo.data.database.ZooDatabase
import com.jacekpietras.zoo.data.repository.AnimalRepositoryImpl
import com.jacekpietras.zoo.data.repository.GpsRepositoryImpl
import com.jacekpietras.zoo.data.repository.MapRepositoryImpl
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val repositoryModule = module {

    single<MapRepository> {
        MapRepositoryImpl(
            context = androidContext(),
        )
    }

    single<GpsRepository> {
        GpsRepositoryImpl(
            context = androidContext(),
            gpsDao = get<ZooDatabase>().gpsDao(),
            gpsHistoryMapper = get(),
        )
    }

    single<AnimalRepository> {
        AnimalRepositoryImpl(
            context = androidContext(),
        )
    }
}