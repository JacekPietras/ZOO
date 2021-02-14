package com.jacekpietras.zoo.data.repository

import android.content.Context
import android.text.format.DateUtils
import com.jacekpietras.core.cutOut
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.data.BuildConfig
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.database.dao.GpsDao
import com.jacekpietras.zoo.data.database.mapper.GpsHistoryMapper
import com.jacekpietras.zoo.data.gps.TxtParser
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.*

internal class GpsRepositoryImpl(
    context: Context,
    private val gpsDao: GpsDao,
    private val gpsHistoryMapper: GpsHistoryMapper,
) : GpsRepository {

    private val debugHistory: Flow<List<List<GpsHistoryEntity>>>

    init {
        debugHistory = if (BuildConfig.DEBUG) {
            val ola1 = TxtParser(context, R.raw.ola_14_02_21)
            val jack1 = TxtParser(context, R.raw.jacek_14_02_21)
            flowOf(ola1.result + jack1.result)
        } else {
            flowOf(emptyList())
        }
    }

    override fun observeLatestPosition(): Flow<GpsHistoryEntity> =
        gpsDao.getLatest().filterNotNull().map(gpsHistoryMapper::from)

    override fun observeAllPositions(): Flow<List<List<GpsHistoryEntity>>> {
        val dbFlow = gpsDao.observeAll().map {
            it.map(gpsHistoryMapper::from)
                .cutOut { prev, next ->
                    next.timestamp - prev.timestamp < DateUtils.MINUTE_IN_MILLIS &&
                            haversine(prev.lon, prev.lat, next.lon, next.lat) < 20
                }
        }
        return if (BuildConfig.DEBUG) {
            combine(debugHistory, dbFlow) { debug, db -> debug + db }
        } else {
            dbFlow
        }
    }

    override suspend fun getAllPositions(): List<GpsHistoryEntity> =
        gpsDao.getAll().map(gpsHistoryMapper::from)

    override suspend fun insertPosition(position: GpsHistoryEntity) {
        gpsDao.insert(gpsHistoryMapper.from(position))
    }
}
