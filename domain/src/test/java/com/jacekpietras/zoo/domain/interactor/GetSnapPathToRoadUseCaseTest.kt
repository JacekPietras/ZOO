package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetSnapPathToRoadUseCaseTest {

    private val getSnappedToRoadUseCase = mock<GetSnappedToRoadUseCase>()
    private val getShortestPathUseCase = mock<GetShortestPathUseCase>()
    private val gGetSnapPathToRoadUseCase = GetSnapPathToRoadUseCase(
        getSnappedToRoadUseCase,
        getShortestPathUseCase,
    )

    @Test
    fun `adds corner points`() = runBlockingTest {
        val point1 = PointD(1.0)
        val point2 = PointD(2.0)
        val point3 = PointD(3.0)
        val point4 = PointD(4.0)
        val pointBetween = PointD(0.0)

        val paths = listOf(
            MapItemEntity.PathEntity(
                listOf(
                    point1,
                    point2,
                    point3,
                    point4,
                )
            )
        )

        whenever(getSnappedToRoadUseCase.run(eq(point1), any())) doReturn point1
        whenever(getSnappedToRoadUseCase.run(eq(point2), any())) doReturn point2
        whenever(getSnappedToRoadUseCase.run(eq(point3), any())) doReturn point3
        whenever(getSnappedToRoadUseCase.run(eq(point4), any())) doReturn point4

        whenever(getShortestPathUseCase.run(eq(point1), eq(point2))) doReturn listOf(point1, pointBetween, point2)
        whenever(getShortestPathUseCase.run(eq(point2), eq(point3))) doReturn listOf(point2, pointBetween, point3)
        whenever(getShortestPathUseCase.run(eq(point3), eq(point4))) doReturn listOf(point3, pointBetween, point4)

        val received = gGetSnapPathToRoadUseCase.run(paths)

        val expected =
            listOf(
                MapItemEntity.PathEntity(
                    listOf(
                        point1,
                        pointBetween,
                        point2,
                        pointBetween,
                        point3,
                        pointBetween,
                        point4,
                    )
                )
            )
        assertEquals(expected, received)
    }
}