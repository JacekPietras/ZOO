package com.jacekpietras.zoo.data.gateway

import android.content.Context
import com.jacekpietras.zoo.domain.feature.sensors.gateway.TrackingServiceGateway
import com.jacekpietras.zoo.tracking.service.TrackingService

internal class TrackingServiceGatewayImpl(private val context: Context) : TrackingServiceGateway {

    override fun start() {
        TrackingService.start(context)
    }
}
