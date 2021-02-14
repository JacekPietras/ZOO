package com.jacekpietras.zoo.data.repository

import android.content.Context
import android.text.format.DateUtils.DAY_IN_MILLIS
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
            val jacek1 = TxtParser(context, R.raw.jacek_14_02_21)
            flowOf(
                listOf(
                    ola1.result.map { it.copy(timestamp = it.timestamp - DAY_IN_MILLIS) },
                    jacek1.result,
                )
            )
        } else {
            flowOf(emptyList())
        }
    }

    override fun observeLatestPosition(): Flow<GpsHistoryEntity> =
        gpsDao.getLatest().filterNotNull().map(gpsHistoryMapper::from)

    override fun observeAllPositions(): Flow<List<List<GpsHistoryEntity>>> =
        if (BuildConfig.DEBUG) {
            val dbFlow = gpsDao.observeAll().map { listOf(it.map(gpsHistoryMapper::from)) }
            combine(debugHistory, dbFlow) { debug, db ->
                debug + db
            }
        } else {
            gpsDao.observeAll().map { listOf(it.map(gpsHistoryMapper::from)) }
        }

    override suspend fun getAllPositions(): List<GpsHistoryEntity> =
        gpsDao.getAll().map(gpsHistoryMapper::from)

    override suspend fun insertPosition(position: GpsHistoryEntity) {
        gpsDao.insert(gpsHistoryMapper.from(position))
    }
}