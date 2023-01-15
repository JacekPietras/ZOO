package com.jacekpietras.zoo.scrapper

import android.app.Application
import com.jacekpietras.zoo.data.di.dataModule
import com.jacekpietras.zoo.domain.di.domainModule
import com.jacekpietras.zoo.scrapper.di.scrapperModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

@Suppress("unused")
class ScrapperApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ScrapperApplication)
            modules(dataModule + domainModule + scrapperModule)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
