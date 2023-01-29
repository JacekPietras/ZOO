package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.ui.opengl.LineArrayD.OutlineD
import com.jacekpietras.mapview.ui.opengl.LineArrayD.StripD

class LinePolygonD(
    val strip: StripD,
    val outline: OutlineD,
)

sealed class LineArrayD(
    open val array: DoubleArray,
) {

    class StripD(
        override val array: DoubleArray,
    ) : LineArrayD(array)

    class OutlineD(
        override val array: DoubleArray,
    ) : LineArrayD(array)
}