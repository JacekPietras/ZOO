package com.jacekpietras.zoo.domain.feature.vrp.algorithms

import com.jacekpietras.zoo.domain.feature.vrp.VRPWithFixedStagesAlgorithm
import com.jacekpietras.zoo.domain.utils.forEachPairIndexed

class MyLinKernighanVRP<T : Any> : VRPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?
    ): List<T> {
        val cache = Cache(
            points = points,
            distanceCalculation = distanceCalculation,
            distanceTable = createWeightArray(points, distanceCalculation),
            tour = IntArray(points.size + 2) { it }.also { it[points.size + 1] = 0 },
        )
        var oldDistance: Double
        var newDistance = cache.getDistance()

        do {
            oldDistance = newDistance
            cache.improve()
            newDistance = cache.getDistance()
        } while (newDistance < oldDistance)

        return cache.tourToPointList()
    }

    private fun Cache<T>.improve() {
        for (i in 0 until size) {
            improve(i)
        }
    }

    private fun Cache<T>.improve(t1: Int, previous: Boolean = false) {
        val t2: Int = if (previous) getPreviousIdx(t1) else getNextIdx(t1)
        val t3: Int = getNearestNeighbor(t2)
        if (t3 != -1 && getDistance(t2, t3) < getDistance(t1, t2)) { // Implementing the gain criteria
            startAlgorithm(t1, t2, t3)
        } else if (!previous) {
            improve(t1, true)
        }
    }

    private fun Cache<T>.getPreviousIdx(index: Int): Int {
        return if (index == 0) size - 1 else index - 1
    }

    private fun Cache<T>.getNextIdx(index: Int): Int {
        return (index + 1) % size
    }

    private fun Cache<T>.getNearestNeighbor(index: Int): Int {
        var minDistance = Double.MAX_VALUE
        var nearestNode = -1
        val actualNode: Int = tour.get(index)
        for (i in 0 until size) {
            if (i != actualNode) {
                val distance: Double = this.distanceTable.get(i).get(actualNode)
                if (distance < minDistance) {
                    nearestNode = getIndex(i)
                    minDistance = distance
                }
            }
        }
        return nearestNode
    }

    private fun Cache<T>.getDistance(n1: Int, n2: Int): Double =
        distanceTable[tour[n1]][tour[n2]]

    private fun Cache<T>.startAlgorithm(t1: Int, t2: Int, t3: Int) {
        val tIndex = ArrayList<Int>()
        tIndex.add(0, -1) // Start with the index 1 to be consistent with Lin-Kernighan Paper
        tIndex.add(1, t1)
        tIndex.add(2, t2)
        tIndex.add(3, t3)
        val initialGain: Double = getDistance(t2, t1) - getDistance(t3, t2) // |x1| - |y1|
        var GStar = 0.0
        var Gi = initialGain
        var k = 3
        var i = 4
        while (true) {
            val newT: Int = selectNewT(tIndex)
            if (newT == -1) {
                break // This should not happen according to the paper
            }
            tIndex.add(i, newT)
            val tiplus1: Int = getNextPossibleY(tIndex)
            if (tiplus1 == -1) {
                break
            }


            // Step 4.f from the paper
            Gi += getDistance(tIndex[tIndex.size - 2], newT)
            if (Gi - getDistance(newT, t1) > GStar) {
                GStar = Gi - getDistance(newT, t1)
                k = i
            }
            tIndex.add(tiplus1)
            Gi -= getDistance(newT, tiplus1)
            i += 2
        }
        if (GStar > 0) {
            tIndex[k + 1] = tIndex[1]
            tour = getTPrime(tIndex, k) // Update the tour
        }
    }

    private fun Cache<T>.getNextPossibleY(tIndex: ArrayList<Int>): Int {
        val ti = tIndex[tIndex.size - 1]
        val ys = ArrayList<Int>()
        for (i in 0 until size) {
            if (!isDisjunctive(tIndex, i, ti)) {
                continue  // Disjunctive criteria
            }
            if (!isPositiveGain(tIndex, i)) {
                continue  // Gain criteria
            }
            if (!nextXPossible(tIndex, i)) {
                continue  // Step 4.f.
            }
            ys.add(i)
        }

        // Get closest y
        var minDistance = Double.MAX_VALUE
        var minNode = -1
        for (i in ys) {
            if (getDistance(ti, i) < minDistance) {
                minNode = i
                minDistance = getDistance(ti, i)
            }
        }
        return minNode
    }

    private fun Cache<T>.nextXPossible(tIndex: ArrayList<Int>, i: Int): Boolean {
        return isConnected(tIndex, i, getNextIdx(i)) || isConnected(tIndex, i, getPreviousIdx(i))
    }

    private fun isConnected(tIndex: ArrayList<Int>, x: Int, y: Int): Boolean {
        if (x == y) return false
        var i = 1
        while (i < tIndex.size - 1) {
            if (tIndex[i] == x && tIndex[i + 1] == y) return false
            if (tIndex[i] == y && tIndex[i + 1] == x) return false
            i += 2
        }
        return true
    }

    private fun Cache<T>.isPositiveGain(tIndex: ArrayList<Int>, ti: Int): Boolean {
        var gain = 0
        for (i in 1 until tIndex.size - 2) {
            val t1 = tIndex[i]
            val t2 = tIndex[i + 1]
            val t3 = if (i == tIndex.size - 3) ti else tIndex[i + 2]
            gain += (getDistance(t2, t3) - getDistance(t1, t2)).toInt() // |yi| - |xi|
        }
        return gain > 0
    }

    private fun Cache<T>.selectNewT(tIndex: ArrayList<Int>): Int {
        val option1: Int = getPreviousIdx(tIndex[tIndex.size - 1])
        val option2: Int = getNextIdx(tIndex[tIndex.size - 1])
        val tour1: IntArray = constructNewTour(tour, tIndex, option1)
        if (isTour(tour1)) {
            return option1
        } else {
            val tour2: IntArray = constructNewTour(tour, tIndex, option2)
            if (isTour(tour2)) {
                return option2
            }
        }
        return -1
    }

    private fun Cache<T>.constructNewTour(tour2: IntArray, tIndex: ArrayList<Int>, newItem: Int): IntArray {
        val changes = ArrayList(tIndex)
        changes.add(newItem)
        changes.add(changes[1])
        return constructNewTour(tour2, changes)
    }

    private fun Cache<T>.isTour(tour: IntArray): Boolean {
        if (tour.size != size) {
            return false
        }
        for (i in 0 until size - 1) {
            for (j in i + 1 until size) {
                if (tour[i] == tour[j]) {
                    return false
                }
            }
        }
        return true
    }

    private fun Cache<T>.getTPrime(tIndex: ArrayList<Int>, k: Int): IntArray {
        val al2 = ArrayList(tIndex.subList(0, k + 2))
        return constructNewTour(tour, al2)
    }

    private fun Cache<T>.constructNewTour(tour: IntArray, changes: ArrayList<Int>): IntArray {
        val currentEdges = deriveEdgesFromTour(tour)
        val X: ArrayList<Edge> = deriveX(changes)
        val Y: ArrayList<Edge> = deriveY(changes)
        var s = currentEdges.size

        // Remove Xs
        for (e in X) {
            for (j in currentEdges.indices) {
                val m: Edge? = currentEdges[j]
                if (e.equals(m)) {
                    s--
                    currentEdges[j] = null
                    break
                }
            }
        }

        // Add Ys
        for (e in Y) {
            s++
            currentEdges.add(e)
        }
        return createTourFromEdges(currentEdges, s)
    }

    private fun createTourFromEdges(currentEdges: ArrayList<Edge?>, s: Int): IntArray {
        val tour = IntArray(s)
        var i = 0
        var last = -1
        while (i < currentEdges.size) {
            val currentEdge = currentEdges[i]
            if (currentEdge != null) {
                tour[0] = currentEdge.endPoint1
                tour[1] = currentEdge.endPoint2
                last = tour[1]
                break
            }
            ++i
        }
        currentEdges[i] = null // remove the edges
        var k = 2
        while (true) {
            // E = find()
            var j = 0
            while (j < currentEdges.size) {
                val e: Edge? = currentEdges[j]
                if (e != null && e.endPoint1 == last) {
                    last = e.endPoint2
                    break
                } else if (e != null && e.endPoint2 == last) {
                    last = e.endPoint1
                    break
                }
                ++j
            }
            // If the list is empty
            if (j == currentEdges.size) break

            // Remove new edge
            currentEdges[j] = null
            if (k >= s) break
            tour[k] = last
            k++
        }
        return tour
    }

    private fun Cache<T>.deriveX(changes: ArrayList<Int>): ArrayList<Edge> {
        val es: ArrayList<Edge> = ArrayList<Edge>()
        var i = 1
        while (i < changes.size - 2) {
            val e = Edge(tour[changes[i]], tour[changes[i + 1]])
            es.add(e)
            i += 2
        }
        return es
    }

    private fun Cache<T>.deriveY(changes: ArrayList<Int>): ArrayList<Edge> {
        val es = ArrayList<Edge>()
        var i = 2
        while (i < changes.size - 1) {
            val e = Edge(tour[changes[i]], tour[changes[i + 1]])
            es.add(e)
            i += 2
        }
        return es
    }

    private fun deriveEdgesFromTour(tour: IntArray): ArrayList<Edge?> {
        val es = ArrayList<Edge?>()
        for (i in tour.indices) {
            val e = Edge(tour[i], tour[(i + 1) % tour.size])
            es.add(e)
        }
        return es
    }

    private fun isDisjunctive(tIndex: ArrayList<Int>, x: Int, y: Int): Boolean {
        if (x == y) return false
        for (i in 0 until tIndex.size - 1) {
            if (tIndex[i] == x && tIndex[i + 1] == y) return false
            if (tIndex[i] == y && tIndex[i + 1] == x) return false
        }
        return true
    }

    private fun Cache<T>.getIndex(node: Int): Int {
        for ((i, t) in tour.withIndex()) {
            if (node == t) {
                return i
            }
        }
        return -1
    }

    private class Cache<T>(
        val points: List<T>,
        val distanceCalculation: suspend (T, T) -> Double,
        val size: Int = points.size,
        val distanceTable: Array<DoubleArray>,
        var tour: IntArray = IntArray(size + 2) { it }.also { it[size + 1] = 0 },
    ) {

        suspend fun getDistance(): Double =
            tourToPointList().zipWithNext { prev, next -> distanceCalculation(prev, next) }.sum()

        fun tourToPointList(): List<T> {
            val n = tour.size - 2
            val shift = tour.indexOf(n)
            return List(n) { points[tour[(it + shift + 1) % (n + 1)]] }
        }
    }

    private suspend fun createWeightArray(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double
    ): Array<DoubleArray> {
        val n = points.size
        val dist = Array(n + 1) { DoubleArray(n + 1) { 0.0 } }

        points.forEachPairIndexed { si, s, ti, t ->
            val weight = distanceCalculation(s, t)
            dist[si][ti] = weight
            dist[ti][si] = weight
        }
        return dist
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): List<T> =
        run(points, distanceCalculation, null)

    private class Edge(
        a: Int,
        b: Int,
    ) : Comparable<Edge> {

        val endPoint1: Int = if (a > b) a else b
        val endPoint2: Int = if (a > b) b else a

        override fun compareTo(other: Edge): Int {
            return if (endPoint1 < other.endPoint1 || endPoint1 == other.endPoint1 && endPoint2 < other.endPoint2) {
                -1
            } else if (this == other) {
                0
            } else {
                1
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Edge

            if (endPoint1 != other.endPoint1) return false
            if (endPoint2 != other.endPoint2) return false

            return true
        }

        override fun hashCode(): Int {
            var result = endPoint1
            result = 31 * result + endPoint2
            return result
        }
    }
}
