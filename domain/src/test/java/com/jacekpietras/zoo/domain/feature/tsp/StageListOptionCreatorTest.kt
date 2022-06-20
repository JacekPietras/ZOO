package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.RegionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StageListOptionCreatorTest {

    private val optionCreator = StageListOptionCreator()

    @Test
    fun `check simple case`() {
        val stages = listOf(
            single("1"),
            single("2"),
            single("3"),
        )

        val received = optionCreator.run(stages).simplify()

        val expected = setOf(
            listOf("1", "2", "3"),
        )
        assertEquals(expected, received)
    }

    @Test
    fun `check with multiple on the end`() {
        val stages = listOf(
            single("1"),
            single("2"),
            multiple("3a", "3b", "3c"),
        )

        val received = optionCreator.run(stages).simplify()

        val expected = setOf(
            listOf("1", "2", "3a"),
            listOf("1", "2", "3b"),
            listOf("1", "2", "3c"),
        )
        assertEquals(expected, received)
    }

    @Test
    fun `check with multiple on beginning`() {
        val stages = listOf(
            multiple("1a", "1b", "1c"),
            single("2"),
            single("3"),
        )

        val received = optionCreator.run(stages).simplify()

        val expected = setOf(
            listOf("1a", "2", "3"),
            listOf("1b", "2", "3"),
            listOf("1c", "2", "3"),
        )
        assertEquals(expected, received)
    }

    @Test
    fun `check with multiple in the middle`() {
        val stages = listOf(
            single("1"),
            multiple("2a", "2b", "2c"),
            single("3"),
        )

        val received = optionCreator.run(stages).simplify()

        val expected = setOf(
            listOf("1", "2a", "3"),
            listOf("1", "2b", "3"),
            listOf("1", "2c", "3"),
        )
        assertEquals(expected, received)
    }

    @Test
    fun `check with multiple multiple times`() {
        val stages = listOf(
            single("1"),
            multiple("2a", "2b", "2c"),
            single("3"),
            multiple("4a", "4b", "4c"),
            multiple("5a", "5b", "5c"),
            single("6"),
        )

        val received = optionCreator.run(stages).simplify()

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
        assertEquals(expected, received)
    }

    private fun single(regionId: String) =
        Stage.Single(
            regionId = RegionId(regionId),
            mutable = true,
        )

    private fun multiple(vararg regionIds: String) =
        Stage.Multiple(
            regionId = RegionId(regionIds.first()),
            alternatives = regionIds.map { RegionId(it) },
            mutable = true,
        )

    private fun List<List<Stage>>.simplify(): Set<List<String>> =
        map { list -> list.filterIsInstance<Stage.InRegion>().map { it.regionId.id } }.toSet()
}
