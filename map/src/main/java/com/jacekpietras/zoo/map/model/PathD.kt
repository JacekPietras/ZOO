package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.PointD

internal class PathD(val vertices: List<PointD>) : DrawableOnCanvas {

    fun toFloat(): PathF =
        PathF(vertices.map { it.toFloat() })
}
