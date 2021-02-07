package com.jacekpietras.zoo.data.di

import com.jacekpietras.zoo.data.gateway.di.gatewayModule
import com.jacekpietras.zoo.data.repository.di.repositoryModule

val dataModule = listOf(repositoryModule, gatewayModule)