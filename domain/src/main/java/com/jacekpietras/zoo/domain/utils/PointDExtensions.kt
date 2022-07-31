package com.jacekpietras.zoo.domain.utils

import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.haversine


internal fun List<PointD>.toLengthInMeters(): Double =
    zipWithNext().sumOf { (p1, p2) -> haversine(p1.x, p1.y, p2.x, p2.y) }