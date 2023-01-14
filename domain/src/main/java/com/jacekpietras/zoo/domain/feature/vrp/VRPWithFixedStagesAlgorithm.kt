package com.jacekpietras.zoo.domain.feature.vrp

internal interface VRPWithFixedStagesAlgorithm<T : Any> : VRPAlgorithm<T> {

    suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>? = null,
    ): List<T>
}