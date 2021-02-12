package com.jacekpietras.zoo.map.model

import android.graphics.PointF
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.map.utils.contains
import com.jacekpietras.zoo.map.utils.containsLine

internal class PolygonF(val vertices: List<PointF>) : DrawableOnCanvas{

    fun contains(point: PointF): Boolean =
        contains(vertices, point)
}
