package com.jacekpietras.zoo.data.cache.di

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.feature.planner.model.PlanEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import kotlinx.coroutines.flow.MutableSharedFlow
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
        MutableStateFlow<List<PathEntity>?>(value = null)
    }
    single(named(MAP_TECHNICAL)) {
        MutableStateFlow<List<PathEntity>?>(value = null)
    }
    single(named(MAP_LINES)) {
        MutableStateFlow<List<PathEntity>?>(value = null)
    }
    single(named(MAP_BUILDINGS)) {
        MutableStateFlow<List<PolygonEntity>?>(value = null)
    }
    single(named(MAP_AVIARY)) {
        MutableStateFlow<List<PolygonEntity>?>(value = null)
    }
    single(named(MAP_FOREST)) {
        MutableStateFlow<List<PolygonEntity>?>(value = null)
    }
    single(named(MAP_TREES)) {
        MutableStateFlow<List<PointD>?>(value = null)
    }
    single(named(MAP_WATER)) {
        MutableStateFlow<List<PolygonEntity>?>(value = null)
    }
    single(named(MAP_VISITED_ROADS)) {
        MutableStateFlow<List<VisitedRoadEdge>>(value = emptyList())
    }
    single(named(OUTSIDE_WORLD_EVENTS)) {
        MutableSharedFlow<Unit>()
    }
    single(named(ARRIVAL_AT_REGION_EVENTS)) {
        MutableSharedFlow<Region>()
    }
    single(named(CURRENT_PLAN)) {
        MutableStateFlow<PlanEntity?>(value = null)
    }
    single(named(MAP_REGIONS)) {
        MutableStateFlow<List<Pair<Region, PolygonEntity>>?>(value = null)
    }
    single(named(MAP_WORLD_RECT)) {
        MutableStateFlow<RectD?>(value = null)
    }
}

const val COMPASS_ENABLED = "CompassEnabledWatcher"
const val LIGHT_SENSOR_ENABLED = "LightSensorEnabledWatcher"
const val NAVIGATION_ENABLED = "NavigationEnabledWatcher"
const val OUTSIDE_WORLD_EVENTS = "OutsideWorldEvents"
const val ARRIVAL_AT_REGION_EVENTS = "ArrivalAtRegionEvents"
const val CURRENT_PLAN = "CurrentPlan"
const val MAP_WORLD_RECT = "MapWorldRect"
const val MAP_ROADS = "MapRoads"
const val MAP_LINES = "MapLines"
const val MAP_TECHNICAL = "MapTechnical"
const val MAP_AVIARY = "MapAviary"
const val MAP_FOREST = "MapForest"
const val MAP_TREES = "MapTrees"
const val MAP_WATER = "MapWater"
const val MAP_BUILDINGS = "MapBuildings"
const val MAP_VISITED_ROADS = "MapVisitedRoads"
const val MAP_REGIONS = "MapRegions"
