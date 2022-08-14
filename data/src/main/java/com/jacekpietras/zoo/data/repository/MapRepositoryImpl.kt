package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.geometry.haversine
import com.jacekpietras.geometry.polygonContains
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
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.random.Random

internal class MapRepositoryImpl(
    private val context: Context,
    private val roadsWatcher: MutableStateFlow<List<PathEntity>?>,
    private val worldRectWatcher: MutableStateFlow<RectD?>,
    private val technicalWatcher: MutableStateFlow<List<PathEntity>?>,
    private val linesWatcher: MutableStateFlow<List<PathEntity>?>,
    private val waterWatcher: MutableStateFlow<List<PolygonEntity>?>,
    private val forestWatcher: MutableStateFlow<List<PolygonEntity>?>,
    private val treesWatcher: MutableStateFlow<List<Pair<PointD, Float>>?>,
    private val buildingsWatcher: MutableStateFlow<List<PolygonEntity>?>,
    private val aviaryWatcher: MutableStateFlow<List<PolygonEntity>?>,
    private val visitedRoadsWatcher: MutableStateFlow<List<VisitedRoadEdge>>,
    private val regionsWatcher: MutableStateFlow<List<Pair<Region, PolygonEntity>>?>,
) : MapRepository {

    private var isMapLoaded = false
    private var visitedRoadsCalculated = false

    override suspend fun loadMap() {
        isMapLoaded = true
        coroutineScope {
            val parser = SvgParser(context, R.xml.map)
            listOf(
                async { regionsWatcher.value = parser.regions },
                async { worldRectWatcher.value = parser.worldRect },
                async { buildingsWatcher.value = (parser.getPointsByGroup("buildings").map(::PolygonEntity)) },
                async { aviaryWatcher.value = (parser.getPointsByGroup("aviary").map(::PolygonEntity)) },
                async { roadsWatcher.value = (parser.getPointsByGroup("paths").map(::PathEntity)) },
                async { technicalWatcher.value = (parser.getPointsByGroup("technical").map(::PathEntity)) },
                async { linesWatcher.value = (parser.getPointsByGroup("lines").map(::PathEntity)) },
                async { waterWatcher.value = (parser.getPointsByGroup("water").map(::PolygonEntity)) },
                async { forestWatcher.value = (parser.getPointsByGroup("forest").map(::PolygonEntity)) },
                async { treesWatcher.value = emptyList() },
            ).awaitAll()

            generateTrees()
        }
    }

    override fun isMapLoaded(): Boolean =
        isMapLoaded

    override fun observeBuildings(): Flow<List<PolygonEntity>> =
        buildingsWatcher.filterNotNull()

    override fun observeAviary(): Flow<List<PolygonEntity>> =
        aviaryWatcher.filterNotNull()

    override fun observeWater(): Flow<List<PolygonEntity>> =
        waterWatcher.filterNotNull()

    override fun observeForest(): Flow<List<PolygonEntity>> =
        forestWatcher.filterNotNull()

    override fun observeTrees(): Flow<List<Pair<PointD, Float>>> =
        treesWatcher.filterNotNull()

    override fun observeRoads(): Flow<List<PathEntity>> =
        roadsWatcher.filterNotNull()

    override suspend fun getRoads(): List<PathEntity> =
        observeRoads().first()

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
        observeTechnicalRoads().first()

    override fun observeLines(): Flow<List<PathEntity>> =
        linesWatcher.filterNotNull()

    override suspend fun getCurrentRegions(): List<Pair<Region, PolygonEntity>> =
        regionsWatcher.filterNotNull().first()

    override fun observeWorldBounds(): Flow<RectD> =
        worldRectWatcher.filterNotNull()

    override suspend fun getWorldBounds(): RectD =
        observeWorldBounds().first()

    private suspend fun generateTrees() {
        treesWatcher.value = observeForest().first().map { forest ->
            val left = forest.left()
            val right = forest.right()
            val top = forest.top()
            val bottom = forest.bottom()
            val width = right - left
            val height = bottom - top
            val widthPositive = width > 0
            val heightPositive = height > 0

            val field = haversine(left, top, right, top) * haversine(left, top, left, bottom)
            val trees = (field * TREE_PER_SQUARE_METER).toInt()

            (0..trees).mapNotNull {
                val x = left + if (widthPositive) random(width) else -random(width)
                val y = top + if (heightPositive) random(height) else -random(height)

                PointD(x, y).takeIf { polygonContains(forest.vertices, it) }
            }.map { it to Random.nextFloat() }
        }.flatten()
    }

    private fun random(num: Double) = Random.nextDouble(abs(num))

    companion object {

        const val TREE_PER_SQUARE_METER = 0.005
    }
}
