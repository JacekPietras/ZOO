package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.tsp.algorithms.City
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

internal class ShortestPathInGeneratedGraphTest {

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
        )
    }

    private fun doTest(
        seed: Long,
        numberOfCities: Int,
        connections: Int,
        bestExpected: Double = 0.0,
    ) = runTest {
        val random = Random(seed)
        val graphAnalyzer = GraphAnalyzer()

        val points = MutableList(numberOfCities) {
            City(
                x = (random.nextDouble() * 500).toInt(),
                y = (random.nextDouble() * 500).toInt(),
            )
        }.toSet().toList()
        val c = points.associateWith { mutableListOf<City>() }

        val roads = (0 until connections).map {
            val next = points.getRandom(random)
            val closest = points.filter { it != next && c[next]?.contains(it) == false }.minByOrNull { it.distanceToCity(next) }!!
            c[next]!!.add(closest)
            c[closest]!!.add(next)

            MapItemEntity.PathEntity(
                listOf(
                    PointD(next.x, next.y),
                    PointD(closest.x, closest.y),
                ),
            )
        }
        graphAnalyzer.initialize(roads, emptyList())

        val start = points.getRandom(random).let { PointD(it.x, it.y) }
        val end = points.getRandom(random).let { PointD(it.x, it.y) }

        val result = measureMap({ println("Calculated in $it") }) {
            graphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
            )
        }
        val distance = result.distance()

        assertEquals(start, result.first())
        assertEquals(end, result.last())
        println("Path length: ${result.size}")

        if (distance < bestExpected) {
            println("New record: $distance")
        } else {
            assertEquals(bestExpected, distance)
        }
    }

    private fun List<PointD>.distance(): Double =
        zipWithNext { a, b -> a.distanceToCity(b) }.sum()

    private fun List<City>.getRandom(random: Random = Random(100)) =
        this[random.nextInt(lastIndex)]

    private fun PointD.distanceToCity(city: PointD): Double =
        sqrt(abs(x - city.x).pow(2.0) + abs(y - city.y).pow(2.0))
}