package com.jacekpietras.zoo.data.repository

import android.content.Context
import android.content.res.XmlResourceParser
import androidx.annotation.XmlRes
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.core.contains
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.xmlpull.v1.XmlPullParser.*


class MapRepositoryImpl(
    context: Context,
) : MapRepository {

    private val worldRect: RectD
    private val regions: List<Pair<String, PolygonEntity>>
    private val buildings: List<List<PointD>>
    private val lines: List<List<PointD>>
    private val paths: List<List<PointD>>

    init {
        val parser = Parser(context, R.xml.map)

        val transformation = getTransformation(parser.rect, parser.worldRect)
        val tags = parser.texts.getValue("tags")
            .map { it.copy(position = transformation(it.position)) }

        worldRect = parser.worldRect
        regions = parser.map.getValue("regions").map { it.map { p -> transformation(p) } }
            .map { region ->
                val containingTags = tags.filter { tag -> contains(region, tag.position) }
                if (containingTags.size != 1) throw IllegalStateException("wrong size of region")
                containingTags.first().content to PolygonEntity(region)
            }
        buildings = parser.map.getValue("buildings").map { it.map { p -> transformation(p) } }
        lines = parser.map.getValue("lines").map { it.map { p -> transformation(p) } }
        paths = parser.map.getValue("paths").map { it.map { p -> transformation(p) } }
    }

    private fun getTransformation(cartesian: RectD, world: RectD): (PointD) -> PointD {
        //  [19.940416, 50.083510] [19.948745, 50.075829]
        val wRatio = world.width() / cartesian.width()
        val hRatio = world.height() / cartesian.height()
        val xShift = world.left - cartesian.left
        val yShift = world.top - cartesian.top

        return { point: PointD ->
            PointD(
                point.x * wRatio + xShift,
                point.y * hRatio + yShift,
            )
        }
    }

    override fun getBuildings(): Flow<List<PolygonEntity>> =
        flowOf(buildings.map(::PolygonEntity))

    override fun getRoads(): Flow<List<PathEntity>> =
        flowOf(paths.map(::PathEntity))

    override fun getLines(): Flow<List<PathEntity>> =
        flowOf(lines.map(::PathEntity))

    override fun getCurrentRegions(): List<Pair<String, PolygonEntity>> =
        regions

    override fun getWorldBounds(): Flow<RectD> =
        flowOf(worldRect)

    private fun <T> List<Pair<String, T>>.getValue(key: String): List<T> =
        filter { it.first == key }.map { it.second }

    private data class Tag(val position: PointD, val content: String)

    private class Parser(context: Context, @XmlRes xmlRes: Int) {
        lateinit var rect: RectD
            private set
        val map = mutableListOf<Pair<String, List<PointD>>>()
        val texts = mutableListOf<Pair<String, Tag>>()
        val worldRect: RectD

        private var group: String? = null
        private var coords = ""

        //        <rect x="808.551" y="176.785" transform="matrix(0.7336 -0.6796 0.6796 0.7336 94.3205 598.8163)" fill="#ED1F24" width="4.645" height="4.659"/>
        init {
            with(context.resources.getXml(xmlRes)) {
                while (eventType != END_DOCUMENT) {
                    when (eventType) {
                        START_TAG -> {
                            when (name) {
                                GROUP_TAG -> group = attr("id")
                                MAIN_TAG -> rect = attr("viewBox").parseBox()
                                POLYGON_TAG,
                                POLYLINE_TAG -> map.add(group!! to attr("points").parsePoly())
                                RECT_TAG -> {
                                    val rect = RectD(
                                        attrD("x"),
                                        attrD("y"),
                                        attrD("x") + attrD("width"),
                                        attrD("y") + attrD("height")
                                    )
                                    map.add(group!! to rect.toPoints())
                                }
                                LINE_TAG -> {
                                    val list = listOf(
                                        PointD(attrD("x1"), attrD("y1")),
                                        PointD(attrD("x2"), attrD("y2")),
                                    )
                                    map.add(group!! to list)
                                }
                                TEXT_TAG -> when (group!!) {
                                    "coords" -> coords += nextText() + " "
                                    else -> texts.add(
                                        group!! to Tag(
                                            attr("transform").parsePoint(),
                                            nextText()
                                        )
                                    )
                                }
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
            worldRect = coords.parseBox()
                .run {
                    RectD(
                        left = top,
                        top = left,
                        right = bottom,
                        bottom = right,
                    )
                }
        }

        private fun String.parsePoly(): List<PointD> =
            this
                .replace(pattern, " ")
                .trim()
                .split(" ")
                .map { it.split(",").run { PointD(get(0).toDouble(), get(1).toDouble()) } }

        private fun String.parsePoint(): PointD =
            this
                .replace("matrix(1 0 0 1 ", "")
                .replace(")", "")
                .split(" ")
                .run { PointD(get(0).toDouble(), get(1).toDouble()) }

        private fun String.parseBox(): RectD =
            this
                .replace(",", " ")
                .replace(pattern, " ")
                .trim()
                .split(" ")
                .map { it.toDouble() }
                .run { RectD(get(0), get(1), get(2), get(3)) }


        private fun XmlResourceParser.attr(name: String): String =
            getAttributeValue(null, name)

        private fun XmlResourceParser.attrD(name: String): Double =
            getAttributeValue(null, name).toDouble()

        private companion object {
            const val GROUP_TAG = "g"
            const val MAIN_TAG = "svg"
            const val POLYGON_TAG = "polygon"
            const val POLYLINE_TAG = "polyline"
            const val RECT_TAG = "rect"
            const val LINE_TAG = "line"
            const val TEXT_TAG = "text"
            private val pattern = "\\s+".toRegex()
        }
    }
}