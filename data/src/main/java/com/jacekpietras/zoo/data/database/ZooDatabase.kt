package com.jacekpietras.zoo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jacekpietras.zoo.data.database.ZooDatabase.Companion.VERSION
import com.jacekpietras.zoo.data.database.dao.GpsDao
import com.jacekpietras.zoo.data.database.model.GpsHistoryDto

@Database(
    entities = [
        GpsHistoryDto::class,
    ],
    version = VERSION,
    exportSchema = true,
)
//@TypeConverters(
//    SomeConverter::class,
//)
internal abstract class ZooDatabase : RoomDatabase() {

    abstract fun gpsDao(): GpsDao

    companion object {

        const val NAME = "zoo.db"
        const val VERSION = 1
    }
}