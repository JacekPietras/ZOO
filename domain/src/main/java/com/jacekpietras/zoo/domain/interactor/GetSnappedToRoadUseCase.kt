package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.GraphAnalyzer
import com.jacekpietras.zoo.domain.model.Snapped

internal class GetSnappedToRoadUseCase {

    suspend fun run(
        point: PointD,
        technicalAllowed: Boolean = false,
    ): Snapped =
        GraphAnalyzer.getSnappedPointWithContext(point, technicalAllowed)
}