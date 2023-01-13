package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.assertExistingRoute
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.assertIsNeighbour
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.distance
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.generateGraph
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.generatePoint
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.getRandom
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.toGraph
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class ParallelGraphAnalyzerTest {

    @Nested
    @DisplayName("Problematic Generated tests")
    inner class ProblematicGeneratedTests {

        @Test
        fun `find shortest path 1`() = runTest {
            doTest(
                seed = 1482803,
                numberOfCities = 5,
                connections = 10,
            )
        }

        @Test
        fun `find shortest path 2`() = runTest {
            doTest(
                seed = 170793,
                numberOfCities = 5,
                connections = 10,
            )
        }

        @Test
        fun `find shortest path 3`() = runTest {
            doTest(
                seed = 2502496,
                numberOfCities = 5,
                connections = 10,
            )
        }

        @Test
        fun `find shortest path 4`() = runTest {
            doTest(
                seed = 698,
                numberOfCities = 1000,
                connections = 2000,
            )
        }

        @Test
        fun `find shortest path 5`() = runTest {
            doTest(
                seed = 306,
                numberOfCities = 1000,
                connections = 2000,
            )
        }

        @Test
        fun `find shortest path 6`() = runTest {
            doTest(
                seed = 1030,
                numberOfCities = 1000,
                connections = 2000,
            )
        }

        @Test
        fun `find shortest path 7`() = runTest {
            doTest(
                seed = 3124,
                numberOfCities = 1000,
                connections = 2000,
            )
        }

        @Test
        fun `find shortest path 8`() = runTest {
            doTest(
                seed = 170793,
                numberOfCities = 5,
                connections = 10,
            )
        }

        @Test
        fun `find shortest path 9`() = runTest {
            doTest(
                seed = 1,
                numberOfCities = 1000,
                connections = 2000,
            )
        }

        @Test
        fun `find shortest path 10`() = runTest {
            doTest(
                seed = 35154,
                numberOfCities = 1000,
                connections = 2000,
            )
        }

        @Test
        fun `find shortest path 11`() = runTest {
            doTest(
                seed = 48349,
                numberOfCities = 1000,
                connections = 2000,
            )
        }

        @Test
        fun `find shortest path 12`() = runTest {
            doTest(
                seed = 117,
                numberOfCities = 5,
                connections = 10,
                startOnGraph = false,
                endOnGraph = false,
            )
        }

        @Test
        fun `find shortest path 13`() = runTest {
            doTest(
                seed = 238,
                numberOfCities = 5,
                connections = 10,
                startOnGraph = false,
                endOnGraph = false,
            )
        }

        @Test
        fun `find shortest path 14`() = runTest {
            doTest(
                seed = 187099,
                numberOfCities = 5,
                connections = 10,
                startOnGraph = false,
                endOnGraph = false,
            )
        }

        @Test
        fun `find shortest path 15`() = runTest {
            doTest(
                seed = 60766,
                numberOfCities = 5,
                connections = 10,
                startOnGraph = false,
                endOnGraph = false,
            )
        }

        @Test
        fun `find shortest path 16`() = runTest {
            doTest(
                seed = 2341,
                numberOfCities = 1000,
                connections = 2000,
                startOnGraph = false,
                endOnGraph = false,
            )
        }

        @Test
        fun `find shortest path 17`() = runTest {
            doTest(
                seed = 5467,
                numberOfCities = 1000,
                connections = 2000,
                startOnGraph = false,
                endOnGraph = false,
                repeat = 1,
                print = true,
            )
        }
    }

//    @Test
//    fun `test generation (multiple) with big graphs`() = runTest {
//        doTests(
//            times = 20_000,
//            seed = 0,
//            numberOfCities = 1000,
//            connections = 2000,
//        )
//    }

//    @Test
//    fun `test generation (multiple) with big graphs and not started on graph`() = runTest {
//        doTests(
//            times = 1_000_000,
//            seed = 5468,
//            numberOfCities = 1000,
//            connections = 2000,
//            startOnGraph = false,
//            endOnGraph = false,
//        )
//    }

//    @Test
//    fun `test generation (multiple) with small graphs`() = runTest {
//        doTests(
//            times = 10_000_000,
//            seed = 0,
//            numberOfCities = 5,
//            connections = 10,
//        )
//    }

//    @Test
//    fun `test generation (multiple) with small graphs and not started on graph`() = runTest {
//        doTests(
//            times = 10_000_000,
//            seed = 0,
//            numberOfCities = 5,
//            connections = 10,
//            startOnGraph = false,
//            endOnGraph = false,
//        )
//    }

    @Nested
    @DisplayName("Simplified edge cases")
    inner class SimplifiedEdgeCases {

        @Test
        fun `start outside graph`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(0, 0),
                    PointD(0, 10),
                ),
                listOf(
                    PointD(0, 0),
                    PointD(5, 5),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(0, 10),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(5, 5),
                    PointD(5, 6),
                ),
            )
            val startPoint = PointD(9, 11)
            val endPoint = PointD(0, 0)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(9, 10),
                PointD(10, 10),
                PointD(5, 5),
                PointD(0, 0),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `start outside graph 2`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(-1, -1),
                    PointD(0, 0),
                    PointD(0, 10),
                ),
                listOf(
                    PointD(0, 0),
                    PointD(5, 5),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(-1, 10),
                    PointD(0, 10),
                    PointD(10, 10),
                    PointD(11, 10),
                ),
                listOf(
                    PointD(5, 5),
                    PointD(5, 6),
                ),
            )
            val startPoint = PointD(9, 11)
            val endPoint = PointD(0, 0)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(9, 10),
                PointD(10, 10),
                PointD(5, 5),
                PointD(0, 0),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `end outside graph`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(0, 0),
                    PointD(0, 10),
                ),
                listOf(
                    PointD(0, 0),
                    PointD(5, 5),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(0, 10),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(5, 5),
                    PointD(5, 6),
                ),
            )
            val startPoint = PointD(0, 0)
            val endPoint = PointD(9, 11)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
                PointD(9, 10),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `end outside graph 2`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(0, 0),
                    PointD(0, 10),
                ),
                listOf(
                    PointD(0, 0),
                    PointD(5, 5),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(0, 10),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(5, 5),
                    PointD(5, 6),
                ),
            )
            val startPoint = PointD(0, 0)
            val endPoint = PointD(1, 10)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(0, 0),
                PointD(0, 10),
                PointD(1, 10),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `end outside graph 3`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(-1, -1),
                    PointD(0, 0),
                    PointD(0, 10),
                ),
                listOf(
                    PointD(0, 0),
                    PointD(5, 5),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(-1, 10),
                    PointD(0, 10),
                    PointD(10, 10),
                    PointD(11, 10),
                ),
                listOf(
                    PointD(5, 5),
                    PointD(5, 6),
                ),
            )
            val startPoint = PointD(0, 0)
            val endPoint = PointD(9, 11)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
                PointD(9, 10),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `end outside graph 4`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(-1, -1),
                    PointD(0, 0),
                    PointD(0, 10),
                ),
                listOf(
                    PointD(0, 0),
                    PointD(5, 5),
                    PointD(10, 10),
                ),
                listOf(
                    PointD(-1, 10),
                    PointD(0, 10),
                    PointD(10, 10),
                    PointD(11, 10),
                ),
                listOf(
                    PointD(5, 5),
                    PointD(5, 6),
                ),
            )
            val startPoint = PointD(0, 0)
            val endPoint = PointD(1, 11)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(0, 0),
                PointD(0, 10),
                PointD(1, 10),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `on straight line`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(0, 0),
                    PointD(0, 5),
                    PointD(0, 10),
                ),
            )
            val startPoint = PointD(1, 2)
            val endPoint = PointD(1, 8)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(0, 2),
                PointD(0, 5),
                PointD(0, 8),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `on straight line, close`() = runTest {
            val roads = listOf(
                listOf(
                    PointD(0, 0),
                    PointD(0, 5),
                    PointD(0, 10),
                ),
            )
            val startPoint = PointD(1, 2)
            val endPoint = PointD(1, 3)

            val result = runShortestPath(roads, startPoint, endPoint)

            val expected = listOf(
                PointD(0, 2),
                PointD(0, 3),
            )
            assertEquals(expected, result)
        }
    }

    private suspend fun runShortestPath(
        roads: List<List<PointD>>,
        startPoint: PointD,
        endPoint: PointD,
        repeat: Int = 1,
        startOnGraph: Boolean = false,
        endOnGraph: Boolean = false,
        print: Boolean = true,
    ): List<PointD> {
        val fullGraphAnalyzer = roads.toGraph()
        val parallelGraphAnalyzer = roads.toGraph()
        var fullGraphFailure: Throwable? = null
        var parallelGraphFailure: Throwable? = null

        val fullResultTimeList = mutableListOf<Duration>()
        val fullResult = (1..repeat).map {
            measureMap({ fullResultTimeList.add(it) }) {
                getShortestPathForTest(
                    graphAnalyzer = fullGraphAnalyzer,
                    startPoint = startPoint,
                    endPoint = endPoint,
                    technicalAllowedAtStart = true,
                    technicalAllowedAtEnd = true,
                )
            }
        }.first()

        try {
            if (startOnGraph && endOnGraph) {
                assertEquals(startPoint, fullResult.first()) { "Incorrect starting point" }
                assertEquals(endPoint, fullResult.last()) { "Incorrect ending point" }
                fullResult.assertExistingRoute(fullGraphAnalyzer)
            } else if (fullResult.size > 2) {
                if (!startOnGraph) {
                    assertIsNeighbour(fullResult[0], fullResult[1], roads)
                }
                fullResult
                    .run { if (!startOnGraph) drop(1) else this }
                    .run { if (!endOnGraph) dropLast(1) else this }
                    .assertExistingRoute(roads)
                if (!endOnGraph) {
                    assertIsNeighbour(fullResult.last(), fullResult[fullResult.lastIndex - 1], roads)
                }
            }
        } catch (failure: Throwable) {
            fullGraphFailure = failure
        }

        val map = mutableMapOf<PointD, String>()
        if (print) {
            printFullGraph(map, parallelGraphAnalyzer.waitForNodes())
            println("\n-------------\n")
            println("Expected: ${fullResult.joinToString { (map[it] ?: "") + "(" + it.x.toInt() + "," + it.y.toInt() + ")" }}\n")
        }

        val resultTimeList = mutableListOf<Duration>()
        val result = (1..repeat).map {
            measureMap({ resultTimeList.add(it) }) {
                parallelGraphAnalyzer.getShortestPathParallel(
                    startPoint = startPoint,
                    endPoint = endPoint,
                    technicalAllowedAtStart = true,
                    technicalAllowedAtEnd = true,
                )
            }
        }.first()

        try {
            if (startOnGraph && endOnGraph) {
                assertEquals(startPoint, result.first()) { "Incorrect starting point" }
                assertEquals(endPoint, result.last()) { "Incorrect ending point" }
                result.assertExistingRoute(parallelGraphAnalyzer)
            } else if (result.size > 2) {
                if (!startOnGraph) {
                    assertIsNeighbour(result[0], result[1], roads)
                }
                result
                    .run { if (!startOnGraph) drop(1) else this }
                    .run { if (!endOnGraph) dropLast(1) else this }
                    .assertExistingRoute(roads)
                if (!endOnGraph) {
                    assertIsNeighbour(result.last(), result[result.lastIndex - 1], roads)
                }
            }
        } catch (failure: Throwable) {
            parallelGraphFailure = failure
        }

        when {
            fullGraphFailure != null && parallelGraphFailure != null -> throw FailedOnBothGraphVerification()
            fullGraphFailure != null -> throw FailedOnFullGraphVerification()
            parallelGraphFailure != null -> throw parallelGraphFailure
        }

        val fullResultTime = fullResultTimeList.average()
        val resultTime = resultTimeList.average()
        if (print) {
            if (resultTime < fullResultTime) {
                println("Calculated in $resultTime, (${fullResultTime - resultTime} faster)")
            } else {
                println("Calculated in $resultTime, (${resultTime - fullResultTime} slower!)")
            }
        }
        if (different(fullResult.distance(), result.distance())) {
            val diffSign = when {
                fullResult.distance() < result.distance() -> " < "
                fullResult.distance() > result.distance() -> " > "
                else -> ", "
            }
            assertEquals(
                fullResult.map { (map[it] ?: "") + (it.x.toInt() to it.y.toInt()) },
                result.map { (map[it] ?: "") + (it.x.toInt() to it.y.toInt()) }) {
                "Result from Full Graph is different\n" +
                        "Full distance: ${fullResult.distance()}${diffSign}Parallel distance: ${result.distance()}\n" +
                        "Full length: ${fullResult.size}, Parallel length: ${result.size}\n"
            }
        }
        return result
    }

    private suspend fun doTest(
        seed: Long,
        numberOfCities: Int,
        connections: Int,
        bestExpected: Double = 99999999.0,
        repeat: Int = 3,
        startOnGraph: Boolean = true,
        endOnGraph: Boolean = true,
        print: Boolean = numberOfCities < 20,
    ) {
        val random = Random(seed)
        val (points, roads) = generateGraph(
            random,
            numberOfCities,
            connections,
        )
        val start = if (startOnGraph) points.getRandom(random).let { PointD(it.x, it.y) }
        else generatePoint(random)
        val end = if (endOnGraph) points.getRandom(random).let { PointD(it.x, it.y) }
        else generatePoint(random)
        val result = try {
            runShortestPath(
                roads,
                start,
                end,
                repeat,
                startOnGraph,
                endOnGraph,
                print,
            )
        } catch (onFull: FailedOnFullGraphVerification) {
            println("Failed on FullGraph Verification Only")
            return
        } catch (onFull: FailedOnBothGraphVerification) {
            println("Failed on Both Graph Verifications")
            return
        }

        val distance = result.distance()
        if (bestExpected >= 0) {
            if (distance < bestExpected) {
                println("New record: $distance")
            } else {
                assertEquals(bestExpected, distance)
            }
        }
    }

    @Suppress("unused")
    private suspend fun doTests(
        seed: Long,
        numberOfCities: Int,
        connections: Int,
        times: Int = 1,
        startOnGraph: Boolean = true,
        endOnGraph: Boolean = true,
        print: Boolean = false,
    ) {
        (0 until times).forEach { i ->
            println("For seed ${seed + i}")
            doTest(
                seed = seed + i,
                numberOfCities = numberOfCities,
                connections = connections,
                bestExpected = -1.0,
                repeat = 1,
                startOnGraph = startOnGraph,
                endOnGraph = endOnGraph,
                print = print,
            )
        }
    }

    private fun List<Duration>.average() =
        map { it.inWholeNanoseconds }.average().toDuration(DurationUnit.NANOSECONDS)

    private fun printFullGraph(map: MutableMap<PointD, String>, nodes: Collection<Node>) {
        var letter: String? = null
        fun toLetter(point: PointD) =
            (if (map[point] != null) {
                map[point]!!
            } else {
                (letter?.next() ?: "A")
                    .also {
                        letter = it
                        map[point] = it
                    }
            }) + "(" + point.x.toInt() + "," + point.y.toInt() + ")"

        println(nodes.joinToString("\n") { node ->
            toLetter(node.point) + "\nedges:\n" + node.edges.joinToString("\n") { edge ->
                " -> " +
                        toLetter(edge.node.point)
            } + "\n"
        })
    }

    private fun different(a: Double, b: Double): Boolean =
        abs(a - b) > (a / 1_000_000)

    private suspend fun List<PointD>.assertExistingRoute(graph: GraphAnalyzer) {
        zipWithNext { a, b ->
            val foundConnection = graph
                .waitForNodes()
                .allEdges()
                .map { it.first.point to it.second.point }
                .any { (v1, v2) -> a == v1 && b == v2 || a == v2 && b == v1 }
            Assertions.assertNotNull(foundConnection) { "Not found connection $a <-> $b" }
        }
    }

    private fun String.next(): String {
        var carry = true
        val result = this.reversed().asIterable().map {
            if (carry) {
                if (it == 'Z') {
                    'A'
                } else {
                    carry = false
                    it + 1
                }
            } else {
                it
            }
        }.reversed()
        return if (carry) {
            "A" + result.map { 'A' }.joinToString("")
        } else {
            result.joinToString("")
        }
    }


    internal suspend fun getShortestPathForTest(
        graphAnalyzer: GraphAnalyzer,
        endPoint: PointD,
        startPoint: PointD?,
        technicalAllowedAtStart: Boolean = true,
        technicalAllowedAtEnd: Boolean = false,
    ): List<PointD> {
        val snapper = PointSnapper()
        val nodes = graphAnalyzer.waitForNodes()

        if (startPoint == null) return listOf(endPoint)
        if (startPoint == endPoint) return listOf(endPoint)
        if (nodes.isEmpty()) return listOf(endPoint)

        val snapStart = snapper.getSnappedOnEdge(nodes, startPoint, technicalAllowed = technicalAllowedAtStart)
        val snapEnd = snapper.getSnappedOnEdge(nodes, endPoint, technicalAllowed = technicalAllowedAtEnd)

        // guarantee good stable result in tests
        val snapStart2 = snapStart.let { graphAnalyzer.makeNode(it) }
        val snapEnd2 = if (isCommonEdge(snapEnd.near1, snapEnd.near2)) {
            snapEnd.let { graphAnalyzer.makeNode(it) }
        } else if (isCommonEdge(snapEnd.near1, snapStart2.node)) {
            snapEnd.copy(near2 = snapStart2.node).let { graphAnalyzer.makeNode(it) }
        } else if (isCommonEdge(snapEnd.near2, snapStart2.node)) {
            snapEnd.copy(near1 = snapStart2.node).let { graphAnalyzer.makeNode(it) }
        } else {
            throw IllegalStateException("Should not happen")
        }

        return Dijkstra(
            vertices = nodes,
            start = snapStart2.node,
            end = snapEnd2.node,
            technicalAllowed = technicalAllowedAtEnd
        )
            .getPath()
            .also { graphAnalyzer.revertConnections(snapStart2, snapEnd2) }
            .map { PointD(it.x, it.y) }
    }

    private fun isCommonEdge(
        n1: Node,
        n2: Node
    ): Boolean =
        n1.edges.any { it.node == n2 }
}
