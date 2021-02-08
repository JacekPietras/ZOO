package com.jacekpietras.zoo

import android.app.Application
import com.jacekpietras.logger.DebugUtilsContextHolder
import com.jacekpietras.logger.LogSupport
import com.jacekpietras.zoo.data.di.dataModule
import com.jacekpietras.zoo.domain.di.domainModule
import com.jacekpietras.zoo.map.di.mapModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

@Suppress("unused")
class ZooApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ZooApplication)
            modules(dataModule + domainModule + mapModule)
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