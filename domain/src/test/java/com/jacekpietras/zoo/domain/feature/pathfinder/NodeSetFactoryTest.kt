package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NodeSetFactoryTest {

    @Test
    fun `just line`() {
        val roads = listOf(
            listOf(
                0 to 0,
                5 to 0,
                10 to 0,
            ),
        )

        val nodes = roads.toNodes()

        val expected = setOf(
            0 to 0,
            5 to 0,
            10 to 0,
        )
        assertEquals(expected.toExpectedNodes(), nodes)
    }

    @Test
    fun `just overlaping line`() {
        val roads = listOf(
            listOf(
                0 to 0,
                5 to 0,
                10 to 0,
            ),
            listOf(
                5 to 10,
                5 to 0,
            ),
        )

        val nodes = roads.toNodes()

        val expected = setOf(
            0 to 0,
            5 to 0,
            5 to 10,
            10 to 0,
        )
        assertEquals(expected.toExpectedNodes(), nodes)

        nodes[0] oneEdgeExist (5 to 0)
        nodes[1] oneEdgeExist (0 to 0)
        nodes[1] oneEdgeExist (5 to 10)
        nodes[1] oneEdgeExist (10 to 0)
        nodes[2] oneEdgeExist (5 to 0)
        nodes[3] oneEdgeExist (5 to 0)
    }

    @Test
    fun `just overlaping line with corner right`() {
        val roads = listOf(
            listOf(
                0 to 0,
                5 to 0,
                10 to 0,
            ),
            listOf(
                5 to 10,
                5 to 0,
                10 to 0,
            ),
        )

        val nodes = roads.toNodes()

        val expected = setOf(
            0 to 0,
            5 to 0,
            5 to 10,
            10 to 0,
        )
        assertEquals(expected.toExpectedNodes(), nodes)

        nodes[0] oneEdgeExist (5 to 0)
        nodes[1] oneEdgeExist (0 to 0)
        nodes[1] oneEdgeExist (5 to 10)
        nodes[1] oneEdgeExist (10 to 0)
        nodes[2] oneEdgeExist (5 to 0)
        nodes[3] oneEdgeExist (5 to 0)
    }

    @Test
    fun `just overlaping line with corner left`() {
        val roads = listOf(
            listOf(
                0 to 0,
                5 to 0,
                10 to 0,
            ),
            listOf(
                5 to 10,
                5 to 0,
                0 to 0,
            ),
        )

        val nodes = roads.toNodes()

        val expected = setOf(
            0 to 0,
            5 to 0,
            5 to 10,
            10 to 0,
        )
        assertEquals(expected.toExpectedNodes(), nodes)

        nodes[0] oneEdgeExist (5 to 0)
        nodes[1] oneEdgeExist (0 to 0)
        nodes[1] oneEdgeExist (5 to 10)
        nodes[1] oneEdgeExist (10 to 0)
        nodes[2] oneEdgeExist (5 to 0)
        nodes[3] oneEdgeExist (5 to 0)
    }

    @Test
    fun `just overlaping line with double line`() {
        val roads = listOf(
            listOf(
                0 to 0,
                5 to 0,
                10 to 0,
            ),
            listOf(
                5 to 10,
                5 to 0,
            ),
            listOf(
                0 to 0,
                10 to 0,
            ),
        )

        val nodes = roads.toNodes()

        val expected = setOf(
            0 to 0,
            5 to 0,
            5 to 10,
            10 to 0,
        )
        assertEquals(expected.toExpectedNodes(), nodes)

        nodes[0] oneEdgeExist (5 to 0)
        nodes[1] oneEdgeExist (0 to 0)
        nodes[1] oneEdgeExist (5 to 10)
        nodes[1] oneEdgeExist (10 to 0)
        nodes[2] oneEdgeExist (5 to 0)
        nodes[3] oneEdgeExist (5 to 0)
        nodes[3] noEdgeExist (0 to 0)
    }

    private fun List<List<Pair<Int, Int>>>.toNodes() =
        NodeSetFactory(this.map { it.map { (x, y) -> PointD(x.toDouble(), y.toDouble()) } }
            .map(MapItemEntity::PathEntity), emptyList())
            .create()
            .sortedWith(compareBy({ it.x }, { it.y }))

    private fun Collection<Pair<Int, Int>>.toExpectedNodes() =
        map { (x, y) -> Node(PointD(x.toDouble(), y.toDouble())) }
            .sortedWith(compareBy({ it.x }, { it.y }))

    private infix fun Node.oneEdgeExist(right: Pair<Int, Int>) {
        assertEquals(1, this.edges.count { edge -> edge.node.point == PointD(right.first.toDouble(), right.second.toDouble()) })
    }
    private infix fun Node.noEdgeExist(right: Pair<Int, Int>) {
        assertEquals(0, this.edges.count { edge -> edge.node.point == PointD(right.first.toDouble(), right.second.toDouble()) })
    }
}