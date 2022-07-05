package com.jacekpietras.zoo.domain.feature.centerpoint

import com.jacekpietras.geometry.PointD

internal object CenterPointFinder {

    fun findCenter(points: List<PointD>): PointD =
        if (points.size <= 4) {
            val x = points.sumOf { it.x } / points.size
            val y = points.sumOf { it.y } / points.size
            PointD(x, y)
        } else {
            val request = arrayOf(points.map { arrayOf(it.x, it.y) }.toTypedArray())
            PolyLabel.polyLabel(request, 0.0000001)
                .let { PointD(it.coordinates[0], it.coordinates[1]) }
        }
}