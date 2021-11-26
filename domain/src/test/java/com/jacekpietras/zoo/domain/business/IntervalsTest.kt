package com.jacekpietras.zoo.domain.business

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class IntervalsTest {

    @Nested
    @DisplayName("Two ranges operations")
    inner class TwoRangesOperations {

        @Test
        fun `sum of separate ranges`() {
            val complex = (0.0..1.0) + (2.0..3.0)

            val expected = Intervals((0.0..1.0), (2.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum of connecting ranges`() {
            val complex = (0.0..1.0) + (1.0..2.0)

            val expected = Intervals((0.0..2.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum of overlapping ranges on the end`() {
            val complex = (0.0..1.0) + (0.5..1.5)

            val expected = Intervals((0.0..1.5))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum of overlapping ranges on the start`() {
            val complex = (0.5..1.5) + (0.0..1.0)

            val expected = Intervals((0.0..1.5))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum with contained range`() {
            val complex = (0.0..2.0) + (0.5..1.5)

            val expected = Intervals((0.0..2.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum with containing range`() {
            val complex = (0.5..1.5) + (0.0..2.0)

            val expected = Intervals((0.0..2.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum of separate ranges, not sorted`() {
            val complex = (2.0..3.0) + (0.0..1.0)

            val expected = Intervals((0.0..1.0), (2.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `diff of separate ranges`() {
            val complex = (0.0..1.0) - (2.0..3.0)

            val expected = Intervals((0.0..1.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `diff of connecting ranges`() {
            val complex = (0.0..1.0) - (1.0..2.0)

            val expected = Intervals((0.0..1.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `diff of containing ranges`() {
            val complex = (0.0..3.0) - (1.0..2.0)

            val expected = Intervals((0.0..1.0), (2.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `diff of overlapping ranges`() {
            val complex = (0.0..1.0) - (0.5..1.5)

            val expected = Intervals((0.0..0.5))
            assertEquals(expected, complex)
        }

        @Test
        fun `diff of separate ranges, not sorted`() {
            val complex = (2.0..3.0) - (0.0..1.0)

            val expected = Intervals((2.0..3.0))
            assertEquals(expected, complex)
        }
    }

    @Nested
    @DisplayName("Multiple ranges operations")
    inner class MultipleRangesOperations {

        @Test
        fun `sum of separate ranges`() {
            val complex = (0.0..1.0) + (2.0..3.0) + (4.0..5.0)

            val expected = Intervals((0.0..1.0), (2.0..3.0), (4.0..5.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum scenario #1`() {
            val complex = (0.0..1.0) + (2.0..3.0) + (0.5..2.0)

            val expected = Intervals((0.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum scenario #2`() {
            val complex = (0.0..1.0) + (2.0..5.0) + (0.5..2.0)

            val expected = Intervals((0.0..5.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum scenario #3`() {
            val complex = (0.5..1.0) + (1.5..2.0) + (0.0..2.5)

            val expected = Intervals((0.0..2.5))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum scenario #4`() {
            val complex = (0.0..2.5) + (0.5..1.0) + (1.5..2.0)

            val expected = Intervals((0.0..2.5))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum scenario #5`() {
            val complex = (0.0..1.0) + (2.0..3.0) + (0.5..2.5)

            val expected = Intervals((0.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum scenario #6`() {
            val complex = (0.0..1.0) + (1.5..2.5) + (2.0..2.0) + (2.0..2.2) + (0.5..3.0)

            val expected = Intervals((0.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `diff scenario #1`() {
            val complex = (0.0..2.0) - (1.0..2.0)

            val expected = Intervals((0.0..1.0))
            assertEquals(expected, complex)
        }
    }

    @Nested
    @DisplayName("Complex ranges operations")
    inner class ComplexRangesOperations {

        @Test
        fun `sum of separate complex ranges`() {
            val complex = Intervals(0.0..1.0) + Intervals(2.0..3.0)

            val expected = Intervals((0.0..1.0), (2.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum of connecting complex ranges`() {
            val complex = Intervals(0.0..1.0) + Intervals(1.0..3.0)

            val expected = Intervals((0.0..3.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `sum of overlapping complex ranges`() {
            val complex = Intervals(0.0..1.0) + Intervals(0.5..1.5)

            val expected = Intervals((0.0..1.5))
            assertEquals(expected, complex)
        }
    }

    @Nested
    @DisplayName("Merge near points")
    inner class MergeNearPoints {

        @Test
        fun `near points scenario #1`() {
            val complex = Intervals(0.0..1.0) + Intervals(1.00000001..2.0)

            val expected = Intervals((0.0..2.0))
            assertEquals(expected, complex)
        }

        @Test
        fun `near points scenario #2`() {
            val complex = Intervals(1.00000001..2.0) + Intervals(0.0..1.0)

            val expected = Intervals((0.0..2.0))
            assertEquals(expected, complex)
        }
    }

    @Nested
    @DisplayName("Compare equals")
    inner class ComapreEquals {

        @Test
        fun `compare scenario #1`() {
            val complex = Intervals(0.0..1.0)

            assertTrue(complex.equals(0.0..1.0))
        }

        @Test
        fun `compare scenario #2`() {
            val complex = Intervals(0.0..2.0)

            assertFalse(complex.equals(0.0..1.0))
        }
    }
}