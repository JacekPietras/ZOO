package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TSPAlgorithm
import com.jacekpietras.zoo.domain.feature.tsp.TSPWithFixedStagesAlgorithm

class MyOldTwoOptHeuristicTSP<T : Any> : TSPAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double
    ): List<T> {
        val n = points.size
        val dist = createWeightArray(points, distanceCalculation)
        val minCostImprovement = 1.0E-8
        val tour = IntArray(n + 1) { it }
        tour[n] = 0

        while (true) {
            var minChange: Double = -minCostImprovement
            var mini = -1
            var minj = -1
            for (i in 0 until n - 2) {
                for (j in i + 2 until n) {
                    val ci = tour[i]
                    val ci1 = tour[i + 1]
                    val cj = tour[j]
                    val cj1 = tour[j + 1]
                    val change = dist[ci][cj] + dist[ci1][cj1] - dist[ci][ci1] - dist[cj][cj1]
                    if (change < minChange) {
                        minChange = change
                        mini = i
                        minj = j
                    }
                }
            }
            if (mini == -1 || minj == -1) {
                return tour.toPointList(points)
            }
            reverse(tour, mini + 1, minj)
        }
    }

    private inline fun List<T>.forEachPair(block: (Int, T, Int, T) -> Unit) {
        val pointsTo = mapIndexed { index, it -> index to it }.toMutableList()
        forEachIndexed { fromIndex, from ->
            pointsTo.remove(fromIndex to from)
            pointsTo.forEach { (toIndex, to) ->
                block(fromIndex, from, toIndex, to)
            }
        }
    }

    private suspend fun createWeightArray(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double
    ): Array<DoubleArray> {
        val n = points.size
        val dist = Array(n) { DoubleArray(n) }

        points.forEachPair { si, s, ti, t ->
            val weight = distanceCalculation(s, t)
            dist[si][ti] = weight
            dist[ti][si] = weight
        }
        return dist
    }

    private fun reverse(arr: IntArray, from: Int, to: Int) {
        var i = from
        var j = to
        while (i < j) {
            val tmp = arr[j]
            arr[j] = arr[i]
            arr[i] = tmp
            ++i
            --j
        }
    }

    private fun <T> IntArray.toPointList(points: List<T>): List<T> =
        map { points[it] }
}