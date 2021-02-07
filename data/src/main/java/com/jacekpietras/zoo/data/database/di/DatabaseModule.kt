package com.jacekpietras.zoo.data.database.di

import androidx.room.Room
import com.jacekpietras.zoo.data.database.ZooDatabase
import com.jacekpietras.zoo.data.database.ZooDatabase.Companion.NAME
import com.jacekpietras.zoo.data.database.mapper.GpsHistoryMapper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


internal val databaseModule = module {

    single {
        Room.databaseBuilder(androidContext(), ZooDatabase::class.java, NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    factory {
        GpsHistoryMapper()
    }
}