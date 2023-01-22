package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.data.utils.PerformanceClass
import com.jacekpietras.zoo.domain.feature.performance.repository.PerformanceClassRepository

class PerformanceClassRepositoryImpl(
    private val performanceClass: PerformanceClass,
) : PerformanceClassRepository {

    override fun getPerformanceRating(): Boolean =
        performanceClass.getRating()
}