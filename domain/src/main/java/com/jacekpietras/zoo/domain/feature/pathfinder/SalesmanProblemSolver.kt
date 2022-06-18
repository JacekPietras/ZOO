package com.jacekpietras.zoo.domain.feature.pathfinder

interface SalesmanProblemSolver<T> {

    suspend fun run(
        request: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>? = null,
    ):  List<T>
}