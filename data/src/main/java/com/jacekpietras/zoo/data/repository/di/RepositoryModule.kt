package com.jacekpietras.zoo.data.repository.di

import com.jacekpietras.zoo.data.cache.di.*
import com.jacekpietras.zoo.data.database.ZooDatabase
import com.jacekpietras.zoo.data.parser.RegionIdAdapter
import com.jacekpietras.zoo.data.repository.*
import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
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
            roadsWatcher = get(named(MAP_ROADS)),
            technicalWatcher = get(named(MAP_TECHNICAL)),
            linesWatcher = get(named(MAP_LINES)),
            buildingsWatcher = get(named(MAP_BUILDINGS)),
            aviaryWatcher = get(named(MAP_AVIARY)),
            visitedRoadsWatcher = get(named(MAP_VISITED_ROADS)),
        )
    }

    single<GpsRepository> {
        GpsRepositoryImpl(
            context = androidContext(),
            gpsDao = get<ZooDatabase>().gpsDao(),
            gpsHistoryMapper = get(),
            compassEnabledWatcher = get(named(COMPASS_ENABLED)),
            lightSensorEnabledWatcher = get(named(LIGHT_SENSOR_ENABLED)),
        )
    }

    single<AnimalRepository> {
        AnimalRepositoryImpl(
            context = androidContext(),
            moshi = get(),
        )
    }
    single<FavoritesRepository> {
        FavoritesRepositoryImpl(
            favoritesDao = get<ZooDatabase>().favoriteDao(),
        )
    }
    single<PlanRepository> {
        PlanRepositoryImpl(
            planDao = get<ZooDatabase>().planDao(),
            planMapper = get(),
        )
    }
}