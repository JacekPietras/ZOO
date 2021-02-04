package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.LatLon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.cos
import kotlin.math.sin

class GetUserPosition {

    operator fun invoke(): Flow<LatLon> =
        flow {
            while (true) {
                emit(
                    LatLon(
                        lat = 5 + sin(System.currentTimeMillis() / 1000.0),
                        lon = 5 + cos(System.currentTimeMillis() / 1000.0),
                    )
                )
                delay(500)
            }
        }
}