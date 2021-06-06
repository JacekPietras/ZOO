package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer

class GetSnappedToRoadUseCase() {

    fun run(point: PointD): PointD =
        GraphAnalyzer.getSnapped(point)
}