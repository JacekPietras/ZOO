package com.jacekpietras.mapview.model

import com.jacekpietras.core.PointD

class PathD(val vertices: List<PointD>) : DrawableOnCanvas {

    internal fun toFloat(): PathF =
        PathF(vertices.map { it.toFloat() })
}
