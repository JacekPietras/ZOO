package com.jacekpietras.mapview.ui

import java.util.concurrent.atomic.AtomicBoolean

internal object MapRenderConfig {

    var antialiasing = false
    val isDrawing = AtomicBoolean(false)
    var isTriangulated = false
    internal var showTriangles: Boolean = false
}