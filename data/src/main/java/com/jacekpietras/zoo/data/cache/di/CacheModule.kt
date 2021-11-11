package com.jacekpietras.zoo.data.cache.di

import com.jacekpietras.zoo.data.cache.watcher.buildColdWatcher
import org.koin.core.qualifier.named
import org.koin.dsl.module

val cacheModule = module {

    single(named(COMPASS_ENABLED)) {
        buildColdWatcher(initValue = false)
    }
}

const val COMPASS_ENABLED = "CompassEnabledWatcher"
