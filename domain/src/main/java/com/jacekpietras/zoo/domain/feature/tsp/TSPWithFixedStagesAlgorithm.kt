package com.jacekpietras.zoo.domain.feature.tsp

internal interface TSPWithFixedStagesAlgorithm<T : Any> : TSPAlgorithm<T> {

    suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>? = null,
    ): List<T>
}