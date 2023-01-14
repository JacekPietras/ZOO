package com.jacekpietras.zoo.domain.feature.vrp

internal interface VRPAlgorithm<T:Any> {

    suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
    ):  List<T>
}