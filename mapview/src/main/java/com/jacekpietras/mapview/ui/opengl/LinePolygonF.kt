package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.ui.opengl.LineArrayF.OutlineF
import com.jacekpietras.mapview.ui.opengl.LineArrayF.StripF

class LinePolygonF(
    val strip: StripF,
    val outline: OutlineF,
) {

    companion object {

        fun create(size: Int): LinePolygonF =
            LinePolygonF(
                strip = StripF(FloatArray(size)),
                outline = OutlineF(FloatArray(size)),
            )
    }
}

sealed class LineArrayF(
    open val array: FloatArray,
) {

    class StripF(
        override val array: FloatArray,
    ) : LineArrayF(array)

    class OutlineF(
        override val array: FloatArray,
    ) : LineArrayF(array)
}