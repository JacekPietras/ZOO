package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.distance
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.generateGraph
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
                seed = 1000,
                numberOfCities = 100,
                connections = 500,
                bestExpected = 536.0218658192133,
            )
        }

        @Test
        fun `find shortest path 2`() = runTest {
            doTest(
                seed = 2001,
                numberOfCities = 1000,
                connections = 2000,
                bestExpected = 517.6658074221267,
            )
        }

        @Test
        fun `find shortest path 3`() = runTest {
            doTest(
                seed = 3000,
                numberOfCities = 1000,
                connections = 2000,
                bestExpected = 551.1973440977065,
            )
        }

        @Test
        fun `find shortest path 4`() = runTest {
            doTest(
                seed = 4002,
                numberOfCities = 1000,
                connections = 3000,
                bestExpected = 279.78271215563814,
            )
        }

        @Test
        fun `find shortest path 5`() = runTest {
            doTest(
                seed = 5000,
                numberOfCities = 5000,
                connections = 10000,
                bestExpected = 445.50727166942147,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 6`() = runTest {
            doTest(
                seed = 6051,
                numberOfCities = 1000,
                connections = 2000,
                bestExpected = 536.809261073828,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 7`() = runTest {
            doTest(
                seed = 6114,
                numberOfCities = 1000,
                connections = 2000,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 8`() = runTest {
            doTest(
                seed = 6117,
                numberOfCities = 1000,
                connections = 2000,
                bestExpected = 42.166155414412245,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 9`() = runTest {
            doTest(
                seed = 6102,
                numberOfCities = 1000,
                connections = 2000,
                bestExpected = 262.71010868175847,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 10`() = runTest {
            doTest(
                seed = 1207,
                numberOfCities = 10,
                connections = 20,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 11`() = runTest {
            doTest(
                seed = 1228,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 12`() = runTest {
            doTest(
                seed = 3144,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 13`() = runTest {
            doTest(
                seed = 19278,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 14`() = runTest {
            doTest(
                seed = 57052,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 15`() = runTest {
            doTest(
                seed = 563877,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 16`() = runTest {
            doTest(
                seed = 596844,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 17`() = runTest {
            doTest(
                seed = 822450,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 18`() = runTest {
            doTest(
                seed = 847085,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 19`() = runTest {
            doTest(
                seed = 69078,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 20`() = runTest {
            doTest(
                seed = 14555,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 21`() = runTest {
            doTest(
                seed = 75274,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 22`() = runTest {
            doTest(
                seed = 382530,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 23`() = runTest {
            doTest(
                seed = 465829,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 24`() = runTest {
            doTest(
                seed = 911975,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 25`() = runTest {
            doTest(
                seed = 1209988,
                numberOfCities = 5,
                connections = 10,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 26`() = runTest {
            doTest(
                seed = 555,
                numberOfCities = 1000,
                connections = 2000,
                bestExpected = 9999999999.0,
                repeat = 1,
            )
        }

        @Test
        fun `find shortest path 27`() = runTest {
            doTest(
                seed = 9,
                numberOfCities = 5,
                connections = 10,
                startOnGraph = false,
                endOnGraph = false,
                bestExpected = 9999999999.0,
                repeat = 1,
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
//    fun `test generation (multiple) with small graphs`() = runTest {
//        doTests(
//            times = 10000000,
//            seed = 911976,
//            numberOfCities = 5,
//            connections = 10,
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

    @Test
    fun `test generation (multiple) with small graphs and not started on graph`() = runTest {
        doTests(
            times = 10000000,
            seed = 4,
            numberOfCities = 5,
            connections = 10,
            startOnGraph = false,
            endOnGraph = false,
        )
    }

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
        val fullResultTime = fullResultTimeList.average()

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
        bestExpected: Double,
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
        else PointD(
            x = (random.nextDouble() * 500).toInt().toDouble(),
            y = (random.nextDouble() * 500).toInt().toDouble(),
        )
        val end = if (endOnGraph) points.getRandom(random).let { PointD(it.x, it.y) }
        else PointD(
            x = (random.nextDouble() * 500).toInt().toDouble(),
            y = (random.nextDouble() * 500).toInt().toDouble(),
        )
        val result = try {
            runShortestPath(roads, start, end, repeat, print)
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