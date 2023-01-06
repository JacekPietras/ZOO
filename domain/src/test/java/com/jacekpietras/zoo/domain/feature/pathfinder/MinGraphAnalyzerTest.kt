package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.assertExistingRoute
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.distance
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.generateGraph
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.generatePoint
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.getRandom
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.toGraph
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class MinGraphAnalyzerTest {

    @Test
    fun `map to min graph`() = runTest {
        val roads = listOf(
            listOf(
                PointD(1, 1),
                PointD(2, 1),
                PointD(2, 2),
                PointD(3, 3),
            ),
            listOf(
                PointD(2, 2),
                PointD(3, 2),
            ),
            listOf(
                PointD(3, 2),
                PointD(4, 2),
                PointD(5, 2),
                PointD(6, 2),
                PointD(7, 2),
            ),
        )

        val minGraph = roads.toGraph().toMinGraph()
        val resultNodes = minGraph.waitForNodes()

        val expectedTerminalNodes =
            setOf(
                PointD(1, 1),
                PointD(2, 2),
                PointD(3, 3),
                PointD(7, 2),
            )
        assertEquals(expectedTerminalNodes, resultNodes.map(MinNode::point).toSet())
        val expectedCornerNodes =
            setOf(
                PointD(2, 1),
                PointD(3, 2),
                PointD(4, 2),
                PointD(5, 2),
                PointD(6, 2),
            )
        val resultCornerNodes = resultNodes.map { it.edges.cornerPoints() }.flatten().flatten().toSet()
        assertEquals(expectedCornerNodes, resultCornerNodes)
        assertEquals(4, resultNodes.size)
    }

    private fun Collection<MinEdge>.cornerPoints() =
        map { it.corners.map(Pair<PointD, Double>::first) }

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
    }

//    @Test
//    fun `test generation (multiple) with big graphs`() = runTest {
//        doTests(
//            times = 100000,
//            seed = 0,
//            numberOfCities = 1000,
//            connections = 2000,
//        )
//    }

//    @Test
//    fun `test generation (multiple) with big graphs and not started on graph`() = runTest {
//        doTests(
//            times = 100000,
//            seed = 0,
//            numberOfCities = 1000,
//            connections = 2000,
//            startOnGraph = false,
//            endOnGraph = false,
//        )
//    }

//    @Test
//    fun `test generation (multiple) with small graphs`() = runTest {
//        doTests(
//            times = 10000000,
//            seed = 0,
//            numberOfCities = 5,
//            connections = 10,
//        )
//    }

//    @Test
//    fun `test generation (multiple) with small graphs and not started on graph`() = runTest {
//        doTests(
//            times = 10000000,
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
        val minGraphAnalyzer = fullGraphAnalyzer.toMinGraph()
        val fullResultTimeList = mutableListOf<Duration>()
        val fullResult = try {
            (1..repeat).map {
                measureMap({ fullResultTimeList.add(it) }) {
                    fullGraphAnalyzer.getShortestPath(
                        startPoint = startPoint,
                        endPoint = endPoint,
                        technicalAllowedAtStart = true,
                        technicalAllowedAtEnd = true,
                    )
                }
            }.first()
        } catch (ignored: Throwable) {
            throw FailedOnFullGraph()
        }
        if(startOnGraph && endOnGraph){
            assertEquals(startPoint, fullResult.first())
            assertEquals(endPoint, fullResult.last())
            fullResult.assertExistingRoute(roads)
        }

        val map = mutableMapOf<PointD, Char>()
        if (print) {
            printFullGraph(roads, map, fullGraphAnalyzer.waitForNodes())
            println("\n-------------\n")
            printMinGraph(roads, map, minGraphAnalyzer.waitForNodes())
            println("\n-------------\n")
            println("Expected: ${fullResult.joinToString { (map[it]?.toString() ?: "") + "(" + it.x.toInt() + "," + it.y.toInt() + ")" }}\n")
        }

        val resultTimeList = mutableListOf<Duration>()
        val result = (1..repeat).map {
            measureMap({ resultTimeList.add(it) }) {
                minGraphAnalyzer.getShortestPath(
                    startPoint = startPoint,
                    endPoint = endPoint,
                    technicalAllowedAtStart = true,
                    technicalAllowedAtEnd = true,
                )
            }
        }.first()
        if(startOnGraph && endOnGraph){
            assertEquals(startPoint, result.first())
            assertEquals(endPoint, result.last())
            result.assertExistingRoute(roads)
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

        assertEquals(
            fullResult.map { (map[it]?.toString() ?: "") + (it.x.toInt() to it.y.toInt()) },
            result.map { (map[it]?.toString() ?: "") + (it.x.toInt() to it.y.toInt()) }) {
            "Result from Full Graph is different\n" +
                    "Full distance: ${fullResult.distance()}, Min distance: ${result.distance()}\n" +
                    "Full length: ${fullResult.size}, Min length: ${result.size}\n"
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
        } catch (onFull: FailedOnFullGraph) {
            println("Failed on FullGraph")
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

    private fun GraphAnalyzer.toMinGraph(): MinGraphAnalyzer = runBlocking {
        MinGraphAnalyzer().also { it.initialize(waitForNodes()) }
    }

    private fun List<Duration>.average() =
        map { it.inWholeNanoseconds }.average().toDuration(DurationUnit.NANOSECONDS)

    private fun printMinGraph(roads: List<List<PointD>>, map: MutableMap<PointD, Char>, nodes: Collection<MinNode>) {
        var letter = 'A' - 1
        fun toLetter(point: PointD) =
            if (roads.size > 50) {
                ""
            } else if (map[point] != null) {
                map[point]
            } else {
                letter += 1
                map[point] = letter
                letter
            }.toString() + "(" + point.x.toInt() + "," + point.y.toInt() + ")"

        println(nodes.joinToString("\n") { node ->
            toLetter(node.point) + "\nedges:\n" + node.edges.joinToString("\n") { edge ->
                " -> " +
                        edge.corners.joinToString(", ") { toLetter(it.first) } +
                        " -> " +
                        toLetter(edge.node.point)
            } + "\n"
        })
    }

    private fun printFullGraph(roads: List<List<PointD>>, map: MutableMap<PointD, Char>, nodes: Collection<Node>) {
        var letter = 'A' - 1
        fun toLetter(point: PointD) =
            if (roads.size > 50) {
                ""
            } else if (map[point] != null) {
                map[point]
            } else {
                letter += 1
                map[point] = letter
                letter
            }.toString() + "(" + point.x.toInt() + "," + point.y.toInt() + ")"

        println(nodes.joinToString("\n") { node ->
            toLetter(node.point) + "\nedges:\n" + node.edges.joinToString("\n") { edge ->
                " -> " +
                        toLetter(edge.node.point)
            } + "\n"
        })
    }
}

class FailedOnFullGraph : Throwable()