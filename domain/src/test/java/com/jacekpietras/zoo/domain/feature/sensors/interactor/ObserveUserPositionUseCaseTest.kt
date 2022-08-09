package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ObserveUserPositionUseCaseTest {

    private val mockGpsRepository = mock<GpsRepository>()
    private val observeUserPositionUseCase = ObserveUserPositionUseCase(
        gpsRepository = mockGpsRepository,
    )

    @Test
    fun `check with empty position`() = runTest {

        whenever(mockGpsRepository.observeLatestPosition()).doReturn(flow { })

        val result = observeUserPositionUseCase.run().firstOrNull()

        assertNull(result)
    }
}