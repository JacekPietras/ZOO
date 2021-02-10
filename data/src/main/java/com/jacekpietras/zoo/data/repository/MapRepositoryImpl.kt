package com.jacekpietras.zoo.data.repository

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.RectF
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.domain.model.LatLon
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.xmlpull.v1.XmlPullParser.*
import timber.log.Timber


class MapRepositoryImpl(
    context: Context,
) : MapRepository {

    private val worldSpace: List<Double>
    private val regions: List<Polyline>
    private val buildings: List<Polyline>
    private val lines: List<Polyline>
    private val paths: List<Polyline>

    init {
        Timber.i("Read SVG-------------")
        var group: String? = null
        var svgSpace: List<Double>? = null
        val map = mutableListOf<Pair<String, Polyline>>()
        val texts = mutableListOf<Pair<String, Tag>>()

        with(context.resources.getXml(R.xml.mapa_zabinca)) {
            while (eventType != END_DOCUMENT) {
                when (eventType) {
                    START_TAG -> {
                        when (name) {
                            GROUP_TAG -> group = attr("id")
                            MAIN_TAG -> svgSpace = attr("viewBox").split(" ").map { it.toDouble() }
                            POLYGON_TAG,
                            POLYLINE_TAG -> map.add(group!! to Polyline(attr("points")))
                            LINE_TAG -> {
                                val list = listOf(
                                    attr("x1").toDouble() to attr("y1").toDouble(),
                                    attr("x2").toDouble() to attr("y2").toDouble(),
                                )
                                map.add(group!! to Polyline(list))
                            }
                            TEXT_TAG -> texts.add(group!! to Tag(attr("transform"), nextText()))
                        }
                    }
                    END_TAG -> {
                        when (name) {
                            GROUP_TAG -> group = null
                        }
                    }
                }
                next()
            }
        }

        regions = map.getValue("regions")
        buildings = map.getValue("buildings")
        lines = map.getValue("lines")
        paths = map.getValue("paths")

        val tags = texts.getValue("tags")
        val coords = texts.getValue("coords")
            .map {
                it.content.split(",").map { num -> num.trim().toDouble() }.zipWithNext().first()
            }
        val worldWidth = coords[1].first - coords[0].first
        val worldHeight = coords[1].second - coords[0].second

        worldSpace = svgSpace!!
    }

    override fun getBuildings(): Flow<List<PolygonEntity>> =
        flowOf(
            buildings.map {
                PolygonEntity(it.list.map { point ->
                    LatLon(
                        point.first,
                        point.second,
                    )
                })
            }
        )

    override fun getRoads(): Flow<List<PathEntity>> =
        flowOf(
            paths.map {
                PathEntity(it.list.map { point ->
                    LatLon(
                        point.first,
                        point.second,
                    )
                })
            }
        )

    override fun getWorldSpace(): Flow<RectF> =
        flowOf(
            RectF(
                worldSpace[0].toFloat(),
                worldSpace[1].toFloat(),
                worldSpace[2].toFloat(),
                worldSpace[3].toFloat(),
            )
        )

    private fun XmlResourceParser.attr(name: String): String =
        getAttributeValue(null, name)

    private fun <T> List<Pair<String, T>>.getValue(key: String): List<T> =
        filter { it.first == key }.map { it.second }

    private companion object {
        const val GROUP_TAG = "g"
        const val MAIN_TAG = "svg"
        const val POLYGON_TAG = "polygon"
        const val POLYLINE_TAG = "polyline"
        const val LINE_TAG = "line"
        const val TEXT_TAG = "text"
        private val pattern = "\\s+".toRegex()
    }

    private class Polyline(val list: List<Pair<Double, Double>>) {

        constructor(points: String) : this(
            points
                .replace(pattern, " ")
                .trim()
                .split(" ")
                .map { it.split(",").run { Pair(get(0).toDouble(), get(1).toDouble()) } }
        )
    }

    private class Tag(points: String, val content: String) {

        val position: Pair<Double, Double> = points
            .replace("matrix(1 0 0 1 ", "")
            .replace(")", "")
            .split(" ")
            .map { it.toDouble() }
            .zipWithNext()
            .first()
    }
}