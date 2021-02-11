package com.jacekpietras.zoo.data.repository

import android.content.Context
import android.content.res.XmlResourceParser
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.xmlpull.v1.XmlPullParser.*


class MapRepositoryImpl(
    context: Context,
) : MapRepository {

    private val worldRect: RectD
    private val regions: List<Polyline>
    private val buildings: List<Polyline>
    private val lines: List<Polyline>
    private val paths: List<Polyline>

    init {
        var group: String? = null
        var svgRect: RectD? = null
        val map = mutableListOf<Pair<String, Polyline>>()
        val texts = mutableListOf<Pair<String, Tag>>()

        with(context.resources.getXml(R.xml.mapa_zabinca)) {
            while (eventType != END_DOCUMENT) {
                when (eventType) {
                    START_TAG -> {
                        when (name) {
                            GROUP_TAG -> group = attr("id")
                            MAIN_TAG -> svgRect = attr("viewBox").parseRect()
                            POLYGON_TAG,
                            POLYLINE_TAG -> map.add(group!! to Polyline(attr("points")))
                            LINE_TAG -> {
                                val list = listOf(
                                    PointD(attr("x1").toDouble(), attr("y1").toDouble()),
                                    PointD(attr("x2").toDouble(), attr("y2").toDouble()),
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

        worldRect = texts.getValue("coords").joinToString(" ").parseRect()
        regions = map.getValue("regions")
        buildings = map.getValue("buildings")
        lines = map.getValue("lines")
        paths = map.getValue("paths")
        val tags = texts.getValue("tags")

//        worldSpace = svgSpace!!
    }

    private fun String.parseRect(): RectD =
        this
            .replace(",", " ")
            .replace(pattern, " ")
            .trim()
            .split(" ")
            .map { it.toDouble() }
            .run { RectD(get(0), get(1), get(2), get(3)) }

    override fun getBuildings(): Flow<List<PolygonEntity>> =
        flowOf(buildings.map { PolygonEntity(it.list) })

    override fun getRoads(): Flow<List<PathEntity>> =
        flowOf(paths.map { PathEntity(it.list) })

    override fun getWorldSpace(): Flow<RectD> =
        flowOf(worldRect)

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

    private class Polyline(val list: List<PointD>) {

        constructor(points: String) : this(
            points
                .replace(pattern, " ")
                .trim()
                .split(" ")
                .map { it.split(",").run { PointD(get(0).toDouble(), get(1).toDouble()) } }
        )
    }

    private class Tag(points: String, val content: String) {

        val position: PointD = points
            .replace("matrix(1 0 0 1 ", "")
            .replace(")", "")
            .split(" ")
            .run { PointD(get(0).toDouble(), get(1).toDouble()) }
    }
}