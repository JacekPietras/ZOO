package com.jacekpietras.zoo.data.repository.di

import com.jacekpietras.zoo.data.cache.di.COMPASS_ENABLED
import com.jacekpietras.zoo.data.database.ZooDatabase
import com.jacekpietras.zoo.data.parser.RegionIdAdapter
import com.jacekpietras.zoo.data.repository.AnimalRepositoryImpl
import com.jacekpietras.zoo.data.repository.GpsRepositoryImpl
import com.jacekpietras.zoo.data.repository.MapRepositoryImpl
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val repositoryModule = module {

    single<Moshi> {
        Moshi.Builder()
            .add(RegionIdAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

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
            compassEnabledWatcher = get(named(COMPASS_ENABLED))
        )
    }

    single<AnimalRepository> {
        AnimalRepositoryImpl(
            context = androidContext(),
            moshi = get(),
        )
    }
}