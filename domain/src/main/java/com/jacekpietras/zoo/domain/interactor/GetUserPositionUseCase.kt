package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.LatLon
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetUserPositionUseCase(
    private val gpsRepository: GpsRepository,
) {

    operator fun invoke(): Flow<LatLon> =
        gpsRepository.observeLatestPosition().map { LatLon(it.lat, it.lon) }


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