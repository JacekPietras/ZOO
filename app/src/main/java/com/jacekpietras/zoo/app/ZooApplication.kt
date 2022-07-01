package com.jacekpietras.zoo.app

import android.app.Application
import com.jacekpietras.logger.DebugUtilsContextHolder
import com.jacekpietras.logger.LogSupport
import com.jacekpietras.zoo.BuildConfig
import com.jacekpietras.zoo.app.di.appModule
import com.jacekpietras.zoo.app.logger.FileLogChannel
import com.jacekpietras.zoo.app.logger.FileLoggingTree
import com.jacekpietras.zoo.catalogue.di.catalogueModule
import com.jacekpietras.zoo.data.di.dataModule
import com.jacekpietras.zoo.domain.di.domainModule
import com.jacekpietras.zoo.map.di.mapModule
import com.jacekpietras.zoo.planner.di.plannerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

@Suppress("unused")
class ZooApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ZooApplication)
            modules(dataModule + domainModule + mapModule + catalogueModule + plannerModule + appModule)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        DebugUtilsContextHolder.init(this)
        Timber.plant(FileLoggingTree())
        FileLogChannel.values().forEach(LogSupport::purgeStaleFiles)
    }

    override fun onTerminate() {
        DebugUtilsContextHolder.destroy()
        super.onTerminate()
    }
}