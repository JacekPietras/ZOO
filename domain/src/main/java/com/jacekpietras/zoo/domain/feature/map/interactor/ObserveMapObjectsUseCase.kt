package com.jacekpietras.zoo.domain.feature.map.interactor

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.zoo.domain.feature.map.extensions.combine
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.interactor.ObserveOldTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionCentersUseCase
import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.flow.Flow

class ObserveMapObjectsUseCase(
    private val observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    private val observeBuildingsUseCase: ObserveBuildingsUseCase,
    private val observeAviaryUseCase: ObserveAviaryUseCase,
    private val observeRoadsUseCase: ObserveRoadsUseCase,
    private val observeWaterUseCase: ObserveWaterUseCase,
    private val observeForestUseCase: ObserveForestUseCase,
    private val observeTechnicalRoadsUseCase: ObserveTechnicalRoadsUseCase,
    private val observeOldTakenRouteUseCase: ObserveOldTakenRouteUseCase,
    private val observeRegionCentersUseCase: ObserveRegionCentersUseCase,
    private val observeMapLinesUseCase: ObserveMapLinesUseCase,
) {

    fun run(): Flow<MapObject> = combine(
        observeWorldBoundsUseCase.run(),
        observeBuildingsUseCase.run(),
        observeAviaryUseCase.run(),
        observeWaterUseCase.run(),
        observeForestUseCase.run(),
        observeRoadsUseCase.run(),
        observeMapLinesUseCase.run(),
        observeTechnicalRoadsUseCase.run(),
        observeOldTakenRouteUseCase.run(),
        observeRegionCentersUseCase.run(),
        ::MapObject,
    )

    class MapObject(
        val worldBounds: RectD,
        val buildings: List<PolygonEntity>,
        val aviary: List<PolygonEntity>,
        val water: List<PolygonEntity>,
        val forest: List<PolygonEntity>,
        val roads: List<PathEntity>,
        val lines: List<PathEntity>,
        val technicalRoads: List<PathEntity>,
        val rawOldTakenRoute: List<PathEntity>,
        val regionsWithCenters: List<Pair<Region, PointD>>,
    )
}
