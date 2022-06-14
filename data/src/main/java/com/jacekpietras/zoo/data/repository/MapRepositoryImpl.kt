package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.cache.watcher.Watcher
import com.jacekpietras.zoo.data.parser.SvgParser
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.Region.AnimalRegion
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


internal class MapRepositoryImpl(
    context: Context,
    private val roadsWatcher: Watcher<List<PathEntity>>,
    private val technicalWatcher: Watcher<List<PathEntity>>,
    private val linesWatcher: Watcher<List<PathEntity>>,
    private val buildingsWatcher: Watcher<List<PolygonEntity>>,
    private val aviaryWatcher: Watcher<List<PolygonEntity>>,
    private val visitedRoadsWatcher: Watcher<List<VisitedRoadEdge>>,
) : MapRepository {

    private val parser by lazy { SvgParser(context, R.xml.map) }
    private var isMapLoaded = false

    private val worldRect: RectD by lazy { parser.worldRect }
    private val regions: List<Pair<Region, PolygonEntity>> by lazy { parser.regions }
    private val buildings: List<List<PointD>> by lazy { parser.getPointsByGroup("buildings") }
    private val aviary: List<List<PointD>> by lazy { parser.getPointsByGroup("aviary") }
    private val technical: List<List<PointD>> by lazy { parser.getPointsByGroup("technical") }
    private val lines: List<List<PointD>> by lazy { parser.getPointsByGroup("lines") }
    private val roads: List<List<PointD>> by lazy { parser.getPointsByGroup("paths") }

    private var visitedRoads: List<VisitedRoadEdge>? = null
    private var visitedRoadsCalculated = false

    override suspend fun loadMap() {
        coroutineScope {
            isMapLoaded = true
            parser
            listOf(
                async { buildingsWatcher.notifyUpdated(buildings.map(::PolygonEntity)) },
                async { aviaryWatcher.notifyUpdated(aviary.map(::PolygonEntity)) },
                async { roadsWatcher.notifyUpdated(roads.map(::PathEntity)) },
                async { technicalWatcher.notifyUpdated(technical.map(::PathEntity)) },
                async { linesWatcher.notifyUpdated(lines.map(::PathEntity)) },
            ).awaitAll()
        }
    }

    override fun isMapLoaded(): Boolean =
        isMapLoaded

    override fun observeBuildings(): Flow<List<PolygonEntity>> =
        buildingsWatcher.dataFlow

    override fun observeAviary(): Flow<List<PolygonEntity>> =
        aviaryWatcher.dataFlow

    override fun observeRoads(): Flow<List<PathEntity>> =
        roadsWatcher.dataFlow

    override suspend fun getRoads(): List<PathEntity> =
        roads.map(::PathEntity)

    // todo code those regions in map
    override suspend fun getDarkRegions(): List<Region> =
        listOf(
            AnimalRegion(RegionId("nocny-pawilon"))
        )

    override fun observeVisitedRoads(): Flow<List<VisitedRoadEdge>> =
        visitedRoadsWatcher.dataFlow

    override fun updateVisitedRoads(list: List<VisitedRoadEdge>) {
        visitedRoadsCalculated = true
        visitedRoads = list
        visitedRoadsWatcher.notifyUpdated(list)
    }

    override fun getVisitedRoads(): List<VisitedRoadEdge>? =
        visitedRoads

    override fun areVisitedRoadsCalculated(): Boolean =
        visitedRoadsCalculated

    override fun observeTechnicalRoads(): Flow<List<PathEntity>> =
        technicalWatcher.dataFlow

    override suspend fun getTechnicalRoads(): List<PathEntity> =
        technical.map(::PathEntity)

    override fun observeLines(): Flow<List<PathEntity>> =
        linesWatcher.dataFlow

    override suspend fun getCurrentRegions(): List<Pair<Region, PolygonEntity>> =
        regions

    override fun observeWorldBounds(): Flow<RectD> =
        flowOf(worldRect)

    override fun getWorldBounds(): RectD =
        worldRect
}
