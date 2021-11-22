package com.jacekpietras.zoo.domain.model

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.Node

internal data class Snapped(
    val point: PointD,
    val near1: Node,
    val near2: Node,
)
