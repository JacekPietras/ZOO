package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.ThemeType
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.*

class ObserveSuggestedThemeTypeUseCase(
    private val observeRegionsInUserPositionUseCase: ObserveRegionsInUserPositionUseCase,
    private val startLightSensorUseCase: StartLightSensorUseCase,
    private val stopLightSensorUseCase: StopLightSensorUseCase,
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<ThemeType> {
        return observeRegionsInUserPositionUseCase.run()
            .combine(flow { emit(mapRepository.getDarkRegions()) }) { regions, darkRegions ->
                regions.any { it in darkRegions }
            }
            .distinctUntilChanged()
            .onEach { isDarkRegion ->
                if (isDarkRegion) {
                    startLightSensorUseCase.run()
                } else {
                    stopLightSensorUseCase.run()
                }
            }
            .combine(
                gpsRepository.observeLuminance()
            ) { isDarkRegion, luminance ->
                if (isDarkRegion && luminance < LOW_LUMINANCE) {
                    ThemeType.NIGHT
                } else {
                    ThemeType.DAY
                }
            }
            .distinctUntilChanged()
            .onCompletion { stopLightSensorUseCase.run() }
    }

    private companion object {

        const val LOW_LUMINANCE = 20
    }
}