package com.jacekpietras.zoo.data.gateway.di

import com.jacekpietras.zoo.data.gateway.MailGatewayImpl
import com.jacekpietras.zoo.data.gateway.TrackingServiceGatewayImpl
import com.jacekpietras.zoo.domain.feature.mail.gateway.MailGateway
import com.jacekpietras.zoo.domain.feature.sensors.gateway.TrackingServiceGateway
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val gatewayModule = module {

    factory<MailGateway> {
        MailGatewayImpl(androidContext())
    }
    factory<TrackingServiceGateway> {
        TrackingServiceGatewayImpl(
            context = androidApplication()
        )
    }
}