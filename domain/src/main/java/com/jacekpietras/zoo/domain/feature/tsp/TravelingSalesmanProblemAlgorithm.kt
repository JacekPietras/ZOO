package com.jacekpietras.zoo.domain.feature.tsp

internal interface TravelingSalesmanProblemAlgorithm<T> {

    suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>? = null,
    ):  Pair<Double, List<T>>
}