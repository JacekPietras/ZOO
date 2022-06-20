package com.jacekpietras.zoo.domain.feature.tsp

interface TravelingSalesmanProblemAlgorithm<T> {

    suspend fun run(
        request: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>? = null,
    ):  Pair<Double, List<T>>
}