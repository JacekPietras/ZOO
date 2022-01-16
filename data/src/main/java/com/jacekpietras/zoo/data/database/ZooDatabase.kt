package com.jacekpietras.zoo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jacekpietras.zoo.data.database.ZooDatabase.Companion.VERSION
import com.jacekpietras.zoo.data.database.dao.FavoriteDao
import com.jacekpietras.zoo.data.database.dao.GpsDao
import com.jacekpietras.zoo.data.database.dao.PlanDao
import com.jacekpietras.zoo.data.database.model.FavoriteDto
import com.jacekpietras.zoo.data.database.model.GpsHistoryDto
import com.jacekpietras.zoo.data.database.model.PlanDto

@Database(
    entities = [
        GpsHistoryDto::class,
        FavoriteDto::class,
        PlanDto::class,
    ],
    version = VERSION,
    exportSchema = true,
)
internal abstract class ZooDatabase : RoomDatabase() {

    abstract fun gpsDao(): GpsDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun planDao(): PlanDao

    companion object {

        const val NAME = "zoo.db"
        const val VERSION = 1
    }
}