package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.feature.map.repository.MapRepository
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartLightSensorUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopLightSensorUseCase
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import com.jacekpietras.zoo.domain.model.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class ObserveSuggestedThemeTypeUseCase(
    private val observeRegionsInUserPositionUseCase: ObserveRegionsInUserPositionUseCase,
    private val startLightSensorUseCase: StartLightSensorUseCase,
    private val stopLightSensorUseCase: StopLightSensorUseCase,
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
) {

    fun run(): Flow<Pair<ThemeType, Float>> {
        return combine(
            observeRegionsInUserPositionUseCase.run(),
            darkRegions,
        ) { regions, darkRegions ->
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
                if (isDarkRegion && luminance >= 0 && luminance < LOW_LUMINANCE) {
                    ThemeType.NIGHT
                } else {
                    ThemeType.DAY
                } to luminance
            }
            .onStart { emit(ThemeType.DAY to -1f) }
            .distinctUntilChanged()
            .onCompletion { stopLightSensorUseCase.run() }
    }

    private val darkRegions = flow { emit(mapRepository.getDarkRegions()) }

    private companion object {

        const val LOW_LUMINANCE = 40
    }
}