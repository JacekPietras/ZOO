package com.jacekpietras.zoo.data.cache.di

import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.qualifier.named
import org.koin.dsl.module

val cacheModule = module {

    single(named(COMPASS_ENABLED)) {
        MutableStateFlow(value = false)
    }
    single(named(LIGHT_SENSOR_ENABLED)) {
        MutableStateFlow(value = false)
    }
    single(named(NAVIGATION_ENABLED)) {
        MutableStateFlow(value = false)
    }

    single(named(MAP_ROADS)) {
        MutableStateFlow<List<MapItemEntity.PathEntity>?>(value = null)
    }
    single(named(MAP_TECHNICAL)) {
        MutableStateFlow<List<MapItemEntity.PathEntity>?>(value = null)
    }
    single(named(MAP_LINES)) {
        MutableStateFlow<List<MapItemEntity.PathEntity>?>(value = null)
    }
    single(named(MAP_BUILDINGS)) {
        MutableStateFlow<List<MapItemEntity.PolygonEntity>?>(value = null)
    }
    single(named(MAP_AVIARY)) {
        MutableStateFlow<List<MapItemEntity.PolygonEntity>?>(value = null)
    }
    single(named(MAP_VISITED_ROADS)) {
        MutableStateFlow<List<VisitedRoadEdge>>(value = emptyList())
    }
}

const val COMPASS_ENABLED = "CompassEnabledWatcher"
const val LIGHT_SENSOR_ENABLED = "LightSensorEnabledWatcher"
const val NAVIGATION_ENABLED = "NavigationEnabledWatcher"
const val MAP_ROADS = "MapRoads"
const val MAP_LINES = "MapLines"
const val MAP_TECHNICAL = "MapTechnical"
const val MAP_AVIARY = "MapAviary"
const val MAP_BUILDINGS = "MapBuildings"
const val MAP_VISITED_ROADS = "MapVisitedRoads"
