package com.jacekpietras.mapview.model

import android.graphics.Paint
import com.jacekpietras.core.PointD

internal sealed class PaintHolder {

    class Static(val paint: Paint) : PaintHolder()

    class Dynamic(val block: (zoom: Double, position: PointD) -> Paint) : PaintHolder()
}
