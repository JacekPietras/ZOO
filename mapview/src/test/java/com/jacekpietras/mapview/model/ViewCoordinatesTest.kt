package com.jacekpietras.mapview.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.containsLine
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ViewCoordinatesTest {

    private val center = PointD(x = 19.944554402852376, y = 50.079725999110686)
    private val viewWidth = 1080
    private val viewHeight = 1240

    //rect 19.943274236185708, 50.08119582013791, 19.945834569519043, 50.078256178083464
    private val tested = ViewCoordinates(
        centerGpsCoordinate = center,
        zoom = 0.0012801666666663418,
        viewWidth = viewWidth,
        viewHeight = viewHeight,
    )

    @Test
    fun `center is on screen`() {
        assertTrue(tested.visibleRect.contains(center.x, center.y))
    }

    @Test
    fun `mapped center is in center of screen`() {
        val mapped = tested.transform(center)

        val expected = PointD(viewWidth / 2.0, viewHeight / 2.0)
        assertEquals(expected, mapped)
    }

    @Test
    fun `points are on on screen`() {
        assertTrue(tested.visibleRect.contains(19.944, 50.08))
        assertTrue(tested.visibleRect.contains(19.945, 50.08))
        assertTrue(tested.visibleRect.contains(19.945, 50.079))
    }

    @Test
    fun `points are not on on screen`() {
        assertFalse(tested.visibleRect.contains(119.944, 50.08))
        assertFalse(tested.visibleRect.contains(19.945, -50.08))
        assertFalse(tested.visibleRect.contains(1.945, 50.081))
    }

    @Test
    fun `lines are on on screen`() {
        assertTrue(
            tested.visibleRect.containsLine(
                PointD(19.944, 50.08),
                PointD(19.945, 50.08),
            )
        )
        assertTrue(
            tested.visibleRect.containsLine(
                PointD(119.944, 150.08),
                PointD(19.945, 50.08),
            )
        )
    }

    @Test
    fun `lines are not on on screen`() {
        assertFalse(
            tested.visibleRect.containsLine(
                PointD(119.944, 50.08),
                PointD(119.945, 50.08),
            )
        )
        assertFalse(
            tested.visibleRect.containsLine(
                PointD(19.944, -50.08),
                PointD(19.945, -50.08),
            )
        )
    }

    @Test
    fun `polygon is on screen`() {
        val polygon = PolygonD(
            listOf(
                PointD(19.944, 50.08),
                PointD(19.945, 50.08),
                PointD(19.945, 50.081),
            )
        )

        assertTrue(polygon.intersects(tested.visibleRect))
    }
}