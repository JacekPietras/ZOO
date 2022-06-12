package com.jacekpietras.zoo.data.cache.di

import com.jacekpietras.zoo.data.cache.watcher.buildColdWatcher
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import org.koin.core.qualifier.named
import org.koin.dsl.module

val cacheModule = module {

    single(named(COMPASS_ENABLED)) {
        buildColdWatcher(initValue = false)
    }
    single(named(LIGHT_SENSOR_ENABLED)) {
        buildColdWatcher(initValue = false)
    }

    single(named(MAP_ROADS)) {
        buildColdWatcher<List<MapItemEntity.PathEntity>>()
    }
    single(named(MAP_TECHNICAL)) {
        buildColdWatcher<List<MapItemEntity.PathEntity>>()
    }
    single(named(MAP_LINES)) {
        buildColdWatcher<List<MapItemEntity.PathEntity>>(initValue = emptyList())
    }
    single(named(MAP_BUILDINGS)) {
        buildColdWatcher<List<MapItemEntity.PolygonEntity>>()
    }
    single(named(MAP_AVIARY)) {
        buildColdWatcher<List<MapItemEntity.PolygonEntity>>()
    }
    single(named(MAP_VISITED_ROADS)) {
        buildColdWatcher<List<VisitedRoadEdge>>(initValue = emptyList())
    }
}

const val COMPASS_ENABLED = "CompassEnabledWatcher"
const val LIGHT_SENSOR_ENABLED = "LightSensorEnabledWatcher"
const val MAP_ROADS = "MapRoads"
const val MAP_LINES = "MapLines"
const val MAP_TECHNICAL = "MapTechnical"
const val MAP_AVIARY = "MapAviary"
const val MAP_BUILDINGS = "MapBuildings"
const val MAP_VISITED_ROADS = "MapVisitedRoads"
