package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD

internal class PathD(val vertices: List<PointD>) : DrawableOnCanvas {

    fun toFloat(): PathF =
        PathF(vertices.map { it.toFloat() })
}
