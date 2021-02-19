package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD

class GetSnappedToRoadUseCase {

    operator fun invoke(point: PointD): PointD =
        point
}