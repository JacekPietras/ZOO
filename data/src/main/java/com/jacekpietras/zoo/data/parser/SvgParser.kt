package com.jacekpietras.zoo.data.parser

import android.content.Context
import android.graphics.Matrix
import androidx.annotation.XmlRes
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.core.polygonContains
import com.jacekpietras.zoo.domain.model.MapItemEntity
import org.xmlpull.v1.XmlPullParser

internal class SvgParser(context: Context, @XmlRes xmlRes: Int) {

    val worldRect: RectD
    val regions: List<Pair<String, MapItemEntity.PolygonEntity>>

    private val transformation: (PointD) -> PointD
    private lateinit var rect: RectD
    private val map = mutableListOf<Pair<String, List<PointD>>>()
    private val texts = mutableListOf<Pair<String, Tag>>()
    private var group: String? = null
    private var coords = ""

    init {
        with(context.resources.getXml(xmlRes)) {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
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
                                val matrix = attr("transform").parseMatrix()
                                val points = rect.toPoints().applyMatrix(matrix)
                                map.add(group!! to points)
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
                    XmlPullParser.END_TAG -> {
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

        transformation = getTransformation(rect, worldRect)
        val tags = texts.getValue("tags")
            .map { it.copy(position = transformation(it.position)) }

        regions = map.getValue("regions").map { it.map { p -> transformation(p) } }
            .map { region ->
                val containingTags = tags.filter { tag -> polygonContains(region, tag.position) }
                if (containingTags.size != 1) throw IllegalStateException("wrong size of region")
                containingTags.first().content to MapItemEntity.PolygonEntity(region)
            }
    }

    fun getPointsByGroup(group: String) =
        map.getValue(group).map { it.map { p -> transformation(p) } }

    private fun <T> List<Pair<String, T>>.getValue(key: String): List<T> =
        filter { it.first == key }.map { it.second }

    private fun getTransformation(cartesian: RectD, world: RectD): (PointD) -> PointD {
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

    private fun String.parseMatrix(): Matrix? =
        this
            .takeIf { it.isNotEmpty() }
            ?.run {
                replace("matrix(", "")
                    .replace(")", "")
                    .split(" ")
                    .map { it.toFloat() }
                    .run {
                        val fullArray = floatArrayOf(
                            get(0), get(2), get(4),
                            get(1), get(3), get(5),
                            0f, 0f, 1f
                        )
                        Matrix().apply { setValues(fullArray) }
                    }
            }

    private fun List<PointD>.applyMatrix(matrix: Matrix?): List<PointD> {
        if (matrix == null) return this
        val pointsArray =
            map { listOf(it.x.toFloat(), it.y.toFloat()) }.flatten().toFloatArray()
        matrix.mapPoints(pointsArray)
        return pointsArray.toList().windowed(size = 2, step = 2).map { PointD(it[0], it[1]) }
    }

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

    private data class Tag(val position: PointD, val content: String)
}