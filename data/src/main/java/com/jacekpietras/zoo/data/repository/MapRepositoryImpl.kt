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
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


internal class MapRepositoryImpl(
    context: Context,
    private val roadsWatcher: Watcher<List<PathEntity>>,
    private val technicalWatcher: Watcher<List<PathEntity>>,
    private val linesWatcher: Watcher<List<PathEntity>>,
    private val buildingsWatcher: Watcher<List<PolygonEntity>>,
    private val aviaryWatcher: Watcher<List<PolygonEntity>>,
    private val visitedRoadsWatcher: Watcher<List<VisitedRoadEdge>>,
) : MapRepository {

    private val parser = SvgParser(context, R.xml.map)

    private val worldRect: RectD by lazy {
        parser.worldRect
    }
    private val regions: List<Pair<Region, PolygonEntity>> by lazy {
        parser.regions
    }
    private val buildings: List<List<PointD>> by lazy {
        parser.getPointsByGroup("buildings")
            .also { buildingsWatcher.notifyUpdated(it.map(::PolygonEntity)) }
    }
    private val aviary: List<List<PointD>> by lazy {
        parser.getPointsByGroup("aviary")
            .also { aviaryWatcher.notifyUpdated(it.map(::PolygonEntity)) }
    }
    private val technical: List<List<PointD>> by lazy {
        parser.getPointsByGroup("technical")
            .also { technicalWatcher.notifyUpdated(it.map(::PathEntity)) }
    }
    private val lines: List<List<PointD>> by lazy {
        parser.getPointsByGroup("lines")
            .also { linesWatcher.notifyUpdated(it.map(::PathEntity)) }
    }
    private val roads: List<List<PointD>> by lazy {
        parser.getPointsByGroup("paths")
            .also {
                roadsWatcher.notifyUpdated(it.map(::PathEntity))
            }
    }
    private var visitedRoads: List<VisitedRoadEdge>? = null
        set(value) {
            field = value
            value?.let { visitedRoadsWatcher.notifyUpdated(it) }
        }

    override suspend fun observeBuildings(): Flow<List<PolygonEntity>> =
        buildingsWatcher.dataFlow.also {
            coroutineScope { launch { buildings } }
        }

    override suspend fun observeAviary(): Flow<List<PolygonEntity>> =
        aviaryWatcher.dataFlow.also {
            coroutineScope { launch { aviary } }
        }

    override suspend fun observeRoads(): Flow<List<PathEntity>> =
        roadsWatcher.dataFlow.also {
            coroutineScope { launch { roads } }
        }

    override suspend fun getRoads(): List<PathEntity> =
        roads.map(::PathEntity)

    override suspend fun observeVisitedRoads(): Flow<List<VisitedRoadEdge>> =
        visitedRoadsWatcher.dataFlow

    override fun updateVisitedRoads(list: List<VisitedRoadEdge>) {
        visitedRoads = list
    }

    override fun areVisitedRoadsCalculated(): Boolean =
        visitedRoads != null

    override suspend fun observeTechnicalRoads(): Flow<List<PathEntity>> =
        technicalWatcher.dataFlow.also {
            coroutineScope { launch { technical } }
        }

    override suspend fun getTechnicalRoads(): List<PathEntity> =
        technical.map(::PathEntity)

    override suspend fun observeLines(): Flow<List<PathEntity>> =
        linesWatcher.dataFlow.also {
            coroutineScope { launch { lines } }
        }

    override suspend fun getCurrentRegions(): List<Pair<Region, PolygonEntity>> =
        regions

    override suspend fun observeWorldBounds(): Flow<RectD> =
        flowOf(worldRect)

    override fun getWorldBounds(): RectD =
        worldRect
}
