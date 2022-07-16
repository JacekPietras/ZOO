package com.jacekpietras.zoo.data.repository.di

import com.jacekpietras.zoo.data.cache.di.ARRIVAL_AT_REGION_EVENTS
import com.jacekpietras.zoo.data.cache.di.COMPASS_ENABLED
import com.jacekpietras.zoo.data.cache.di.LIGHT_SENSOR_ENABLED
import com.jacekpietras.zoo.data.cache.di.MAP_AVIARY
import com.jacekpietras.zoo.data.cache.di.MAP_BUILDINGS
import com.jacekpietras.zoo.data.cache.di.MAP_LINES
import com.jacekpietras.zoo.data.cache.di.MAP_ROADS
import com.jacekpietras.zoo.data.cache.di.MAP_TECHNICAL
import com.jacekpietras.zoo.data.cache.di.MAP_VISITED_ROADS
import com.jacekpietras.zoo.data.cache.di.NAVIGATION_ENABLED
import com.jacekpietras.zoo.data.cache.di.OUTSIDE_WORLD_EVENTS
import com.jacekpietras.zoo.data.database.ZooDatabase
import com.jacekpietras.zoo.data.parser.RegionIdAdapter
import com.jacekpietras.zoo.data.repository.AnimalRepositoryImpl
import com.jacekpietras.zoo.data.repository.FavoritesRepositoryImpl
import com.jacekpietras.zoo.data.repository.GpsEventsRepositoryImpl
import com.jacekpietras.zoo.data.repository.GpsRepositoryImpl
import com.jacekpietras.zoo.data.repository.MapRepositoryImpl
import com.jacekpietras.zoo.data.repository.PlanRepositoryImpl
import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository
import com.jacekpietras.zoo.domain.feature.favorites.repository.FavoritesRepository
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.feature.planner.repository.PlanRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsEventsRepository
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
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
            navigationEnabledWatcher = get(named(NAVIGATION_ENABLED)),
        )
    }

    single<GpsEventsRepository> {
        GpsEventsRepositoryImpl(
            outsideWorldEvents = get(named(OUTSIDE_WORLD_EVENTS)),
            arrivalAtRegionEvents = get(named(ARRIVAL_AT_REGION_EVENTS)),
        )
    }

    single<AnimalRepository> {
        AnimalRepositoryImpl(
            context = androidContext(),
            moshi = get(),
        )
    }

    factory<FavoritesRepository> {
        FavoritesRepositoryImpl(
            favoritesDao = get<ZooDatabase>().favoriteDao(),
        )
    }

    factory<PlanRepository> {
        PlanRepositoryImpl(
            planDao = get<ZooDatabase>().planDao(),
            planMapper = get(),
        )
    }
}