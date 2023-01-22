package com.jacekpietras.zoo.domain.feature.performance.interactor

import com.jacekpietras.zoo.domain.feature.performance.repository.PerformanceClassRepository

class GetPerformanceClassUseCase(
    private val performanceClassRepository: PerformanceClassRepository,
) {

    fun run(): Boolean =
        performanceClassRepository.getPerformanceRating()
}