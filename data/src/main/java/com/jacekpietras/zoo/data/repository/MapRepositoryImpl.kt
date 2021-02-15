package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.data.BuildConfig
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.svg.SvgParser
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf


class MapRepositoryImpl(
    context: Context,
) : MapRepository {

    private val worldRect: RectD
    private val regions: List<Pair<String, PolygonEntity>>
    private val buildings: List<List<PointD>>
    private val aviary: List<List<PointD>>
    private val technical: List<List<PointD>>
    private val lines: List<List<PointD>>
    private val roads: List<List<PointD>>

    init {
        val parser = SvgParser(context, R.xml.map)

        worldRect = parser.worldRect
        regions = parser.regions
        buildings = parser.getPointsByGroup("buildings")
        aviary = parser.getPointsByGroup("aviary")
        technical = parser.getPointsByGroup("technical")
        lines = parser.getPointsByGroup("lines")
        roads = parser.getPointsByGroup("paths")
    }

    override fun getBuildings(): Flow<List<PolygonEntity>> =
        flowOf(buildings.map(::PolygonEntity))

    override fun getAviary(): Flow<List<PolygonEntity>> =
        flowOf(aviary.map(::PolygonEntity))

    override fun getRoads(): Flow<List<PathEntity>> =
        if (BuildConfig.DEBUG) {
            flow {
                emit(emptyList())
                delay(5000L)
                emit(roads.map(::PathEntity))
            }
        } else {
            flowOf(roads.map(::PathEntity))
        }

    override fun getTechnicalRoads(): Flow<List<PathEntity>> =
        flowOf(technical.map(::PathEntity))

    override fun getLines(): Flow<List<PathEntity>> =
        flowOf(lines.map(::PathEntity))

    override fun getCurrentRegions(): List<Pair<String, PolygonEntity>> =
        regions

    override fun observeWorldBounds(): Flow<RectD> =
        flowOf(worldRect)

    override fun getWorldBounds(): RectD =
        worldRect
}
