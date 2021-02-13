package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetUserPositionUseCase(
    private val gpsRepository: GpsRepository,
) {

    operator fun invoke(): Flow<PointD> =
        gpsRepository.observeLatestPosition().map { PointD(it.lon, it.lat) }


//        flow {
//            while (true) {
//                emit(
//                    LatLon(
//                        lat = 5 + sin(System.currentTimeMillis() / 1000.0),
//                        lon = 5 + cos(System.currentTimeMillis() / 1000.0),
//                    )
//                )
//                delay(500)
//            }
//        }
}