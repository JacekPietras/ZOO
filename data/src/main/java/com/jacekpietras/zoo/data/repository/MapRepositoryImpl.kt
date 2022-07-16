package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.parser.SvgParser
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.Region.AnimalRegion
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf


internal class MapRepositoryImpl(
    context: Context,
    private val roadsWatcher: MutableStateFlow<List<PathEntity>?>,
    private val technicalWatcher: MutableStateFlow<List<PathEntity>?>,
    private val linesWatcher: MutableStateFlow<List<PathEntity>?>,
    private val buildingsWatcher: MutableStateFlow<List<PolygonEntity>?>,
    private val aviaryWatcher: MutableStateFlow<List<PolygonEntity>?>,
    private val visitedRoadsWatcher: MutableStateFlow<List<VisitedRoadEdge>>,
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

    private var visitedRoadsCalculated = false

    override suspend fun loadMap() {
        coroutineScope {
            isMapLoaded = true
            parser
            listOf(
                async { buildingsWatcher.value = (buildings.map(::PolygonEntity)) },
                async { aviaryWatcher.value = (aviary.map(::PolygonEntity)) },
                async { roadsWatcher.value = (roads.map(::PathEntity)) },
                async { technicalWatcher.value = (technical.map(::PathEntity)) },
                async { linesWatcher.value = (lines.map(::PathEntity)) },
            ).awaitAll()
        }
    }

    override fun isMapLoaded(): Boolean =
        isMapLoaded

    override fun observeBuildings(): Flow<List<PolygonEntity>> =
        buildingsWatcher.filterNotNull()

    override fun observeAviary(): Flow<List<PolygonEntity>> =
        aviaryWatcher.filterNotNull()

    override fun observeRoads(): Flow<List<PathEntity>> =
        roadsWatcher.filterNotNull()

    override suspend fun getRoads(): List<PathEntity> =
        roads.map(::PathEntity)

    // todo code those regions in map
    override suspend fun getDarkRegions(): List<Region> =
        listOf(
            AnimalRegion(RegionId("nocny-pawilon"))
        )

    override fun observeVisitedRoads(): Flow<List<VisitedRoadEdge>> =
        visitedRoadsWatcher

    override fun updateVisitedRoads(list: List<VisitedRoadEdge>) {
        visitedRoadsCalculated = true
        visitedRoadsWatcher.value = list
    }

    override fun getVisitedRoads(): List<VisitedRoadEdge> =
        visitedRoadsWatcher.value

    override fun areVisitedRoadsCalculated(): Boolean =
        visitedRoadsCalculated

    override fun observeTechnicalRoads(): Flow<List<PathEntity>> =
        technicalWatcher.filterNotNull()

    override suspend fun getTechnicalRoads(): List<PathEntity> =
        technical.map(::PathEntity)

    override fun observeLines(): Flow<List<PathEntity>> =
        linesWatcher.filterNotNull()

    override suspend fun getCurrentRegions(): List<Pair<Region, PolygonEntity>> =
        regions

    override fun observeWorldBounds(): Flow<RectD> =
        flowOf(worldRect)

    override fun getWorldBounds(): RectD =
        worldRect
}
