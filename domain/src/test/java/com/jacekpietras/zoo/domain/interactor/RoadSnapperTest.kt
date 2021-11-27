package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.Node
import com.jacekpietras.zoo.domain.business.RoadSnapper
import com.jacekpietras.zoo.domain.business.filterWithPrev
import com.jacekpietras.zoo.domain.model.SnappedOnEdge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class RoadSnapperTest {

    private val getSnapPathToRoadUseCase = RoadSnapper()

    @Test
    fun `test connection 1`() {
        val near = mock<Node>()
        val point1 = SnappedOnEdge(PointD(1.0), near, near)
        val point2 = SnappedOnEdge(PointD(2.0), near, near)
        val point3 = SnappedOnEdge(PointD(3.0), near, near)
        val point4 = SnappedOnEdge(PointD(4.0), near, near)

        val given = listOf(
            listOf(
                point1,
                point2,
            ),
            listOf(
                point2,
                point3,
            ),
            listOf(
                point3,
                point4,
            ),
        )

        val received = getSnapPathToRoadUseCase.connectIfPossible(given)

        val expected = listOf(
            listOf(
                point1,
                point2,
                point3,
                point4,
            )
        )
        assertEquals(expected, received)
    }

    @Test
    fun `test connection 2`() {
        val near = mock<Node>()
        val point1 = SnappedOnEdge(PointD(1.0), near, near)
        val point2 = SnappedOnEdge(PointD(2.0), near, near)
        val point3 = SnappedOnEdge(PointD(3.0), near, near)
        val point4 = SnappedOnEdge(PointD(4.0), near, near)

        val given = listOf(
            listOf(
                point1,
                point2,
            ),
            listOf(
                point3,
                point4,
            ),
        )

        val received = getSnapPathToRoadUseCase.connectIfPossible(given)

        val expected = listOf(
            listOf(
                point1,
                point2,
            ),
            listOf(
                point3,
                point4,
            )
        )
        assertEquals(expected.map { a -> a.map { it.point } }, received.map { a -> a.map { it.point } })
    }

    @Test
    fun `removes duplicates`() {
        val result = listOf(1, 1, 2, 5, 5, 6, 7, 8, 8).filterWithPrev { prev, next -> prev != next }

        assertEquals(listOf(1, 2, 5, 6, 7, 8), result)
    }
}