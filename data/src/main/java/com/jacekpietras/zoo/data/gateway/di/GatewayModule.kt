package com.jacekpietras.zoo.data.gateway.di

import com.jacekpietras.zoo.data.gateway.MailGatewayImpl
import com.jacekpietras.zoo.domain.feature.mail.gateway.MailGateway
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val gatewayModule = module {

    factory<MailGateway> {
        MailGatewayImpl(androidContext())
    }
}