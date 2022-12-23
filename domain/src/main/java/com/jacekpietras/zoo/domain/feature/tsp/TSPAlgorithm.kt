package com.jacekpietras.zoo.domain.feature.tsp

internal interface TSPAlgorithm<T:Any> {

    suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
    ):  List<T>
}