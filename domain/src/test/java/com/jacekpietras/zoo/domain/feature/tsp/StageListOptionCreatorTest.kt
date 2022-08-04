package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.Region.AnimalRegion
import com.jacekpietras.zoo.domain.model.RegionId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StageListOptionCreatorTest {

    private val optionCreator = StageListOptionCreator()

    @Test
    fun `check simple case`() = runTest {
        val stages = listOf(
            single("1"),
            single("2"),
            single("3"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("1", "2", "3"),
        )
        assertEquals(expected, received.simplify())
    }

    @Test
    fun `check with multiple on the end`() = runTest {
        val stages = listOf(
            single("1"),
            single("2"),
            multiple("3a", "3b", "3c"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("1", "2", "3a"),
            listOf("1", "2", "3b"),
            listOf("1", "2", "3c"),
        )
        assertEquals(expected, received.simplify())
    }

    @Test
    fun `check with multiple on beginning`() = runTest {
        val stages = listOf(
            multiple("1a", "1b", "1c"),
            single("2"),
            single("3"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("1a", "2", "3"),
            listOf("1b", "2", "3"),
            listOf("1c", "2", "3"),
        )
        assertEquals(expected, received.simplify())
    }

    @Test
    fun `check with multiple in the middle`() = runTest {
        val stages = listOf(
            single("1"),
            multiple("2a", "2b", "2c"),
            single("3"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("1", "2a", "3"),
            listOf("1", "2b", "3"),
            listOf("1", "2c", "3"),
        )
        assertEquals(expected, received.simplify())
    }

    @Test
    fun `check with multiple that can be skipped at begin`() = runTest {
        val stages = listOf(
            single("b"),
            multiple("a", "b", "c"),
            single("3"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("b", "b", "3"),
        )
        assertEquals(expected, received.simplify())
    }

    @Test
    fun `check with multiple that can be skipped at end`() = runTest {
        val stages = listOf(
            single("1"),
            multiple("a", "b", "c"),
            single("b"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("1", "b", "b"),
        )
        assertEquals(expected, received.simplify())
    }

    @Test
    fun `check with multiple that can be skipped by copying`() = runTest {
        val stages = listOf(
            single("1"),
            multiple("a", "b", "c"),
            multiple("a", "b", "c"),
            single("4"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("1", "a", "a", "4"),
            listOf("1", "b", "b", "4"),
            listOf("1", "c", "c", "4"),
        )
        assertEquals(expected, received.simplify())
    }

    @Test
    fun `check with multiple multiple times`() = runTest {
        val stages = listOf(
            single("1"),
            multiple("2a", "2b", "2c"),
            single("3"),
            multiple("4a", "4b", "4c"),
            multiple("5a", "5b", "5c"),
            single("6"),
        )

        val received = mutableListOf<List<Stage>>()
        optionCreator.run(stages, { received.add(it) })

        val expected = setOf(
            listOf("1", "2a", "3", "4a", "5a", "6"),
            listOf("1", "2b", "3", "4a", "5a", "6"),
            listOf("1", "2c", "3", "4a", "5a", "6"),
            listOf("1", "2a", "3", "4b", "5a", "6"),
            listOf("1", "2b", "3", "4b", "5a", "6"),
            listOf("1", "2c", "3", "4b", "5a", "6"),
            listOf("1", "2a", "3", "4c", "5a", "6"),
            listOf("1", "2b", "3", "4c", "5a", "6"),
            listOf("1", "2c", "3", "4c", "5a", "6"),
            listOf("1", "2a", "3", "4a", "5b", "6"),
            listOf("1", "2b", "3", "4a", "5b", "6"),
            listOf("1", "2c", "3", "4a", "5b", "6"),
            listOf("1", "2a", "3", "4b", "5b", "6"),
            listOf("1", "2b", "3", "4b", "5b", "6"),
            listOf("1", "2c", "3", "4b", "5b", "6"),
            listOf("1", "2a", "3", "4c", "5b", "6"),
            listOf("1", "2b", "3", "4c", "5b", "6"),
            listOf("1", "2c", "3", "4c", "5b", "6"),
            listOf("1", "2a", "3", "4a", "5c", "6"),
            listOf("1", "2b", "3", "4a", "5c", "6"),
            listOf("1", "2c", "3", "4a", "5c", "6"),
            listOf("1", "2a", "3", "4b", "5c", "6"),
            listOf("1", "2b", "3", "4b", "5c", "6"),
            listOf("1", "2c", "3", "4b", "5c", "6"),
            listOf("1", "2a", "3", "4c", "5c", "6"),
            listOf("1", "2b", "3", "4c", "5c", "6"),
            listOf("1", "2c", "3", "4c", "5c", "6"),
        )
        assertEquals(expected, received.simplify())
    }

    private fun single(regionId: String) =
        Stage.Single(
            region = AnimalRegion(RegionId(regionId)),
            mutable = true,
            seen = false,
        )

    private fun multiple(vararg regionIds: String) =
        Stage.Multiple(
            region = AnimalRegion(RegionId(regionIds.first())),
            alternatives = regionIds.map { AnimalRegion(RegionId(it)) },
            mutable = true,
            seen = false,
        )

    private fun List<List<Stage>>.simplify(): Set<List<String>> =
        map { list -> list.filterIsInstance<Stage.InRegion>().map { it.region.id.id } }.toSet()
}
