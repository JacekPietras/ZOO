package com.jacekpietras.zoo.data.repository

import android.content.Context
import android.text.format.DateUtils
import com.jacekpietras.core.cutOut
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.data.BuildConfig
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.cache.watcher.Watcher
import com.jacekpietras.zoo.data.database.dao.GpsDao
import com.jacekpietras.zoo.data.database.mapper.GpsHistoryMapper
import com.jacekpietras.zoo.data.parser.TxtParser
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class GpsRepositoryImpl(
    private val context: Context,
    private val gpsDao: GpsDao,
    private val gpsHistoryMapper: GpsHistoryMapper,
    private val compassEnabledWatcher: Watcher<Boolean>,
) : GpsRepository {

    private val compass = MutableStateFlow(0f)

    private val debugHistory by lazy {
        if (BuildConfig.DEBUG) {
            val ola1 = TxtParser(context, R.raw.ola_14_02_21)
            val jack1 = TxtParser(context, R.raw.jacek_14_02_21)
            val ola2 = TxtParser(context, R.raw.ola_28_02_21)
            val jack2 = TxtParser(context, R.raw.jacek_28_02_21)
            val ola3 = TxtParser(context, R.raw.ola_08_05_21)
            val jack3 = TxtParser(context, R.raw.jacek_08_05_21)
            val eliza3 = TxtParser(context, R.raw.eliza_08_05_21)

            ola1.result// + jack1.result
//                        + jack2.result + ola2.result
//                        + jack3.result + ola3.result + eliza3.result

        } else {
            emptyList()
        }
    }

    override fun observeLatestPosition(): Flow<GpsHistoryEntity> =
        gpsDao.getLatest().filterNotNull().map(gpsHistoryMapper::from)

    override fun observeAllPositions(): Flow<List<List<GpsHistoryEntity>>> {
        return gpsDao.observeAll().map {
            it.map(gpsHistoryMapper::from)
                .cutOut { prev, next ->
                    next.timestamp - prev.timestamp < DateUtils.MINUTE_IN_MILLIS &&
                            haversine(prev.lon, prev.lat, next.lon, next.lat) < 20
                }
        }
    }

    override fun observeOldPositions(): Flow<List<List<GpsHistoryEntity>>> =
        flowOf(debugHistory)

    override suspend fun getOldPositions(): List<List<GpsHistoryEntity>> =
        debugHistory

    override suspend fun getAllPositionsNormalized(): List<List<GpsHistoryEntity>> =
        gpsDao.getAll().map(gpsHistoryMapper::from)
            .cutOut { prev, next ->
                next.timestamp - prev.timestamp < DateUtils.MINUTE_IN_MILLIS &&
                        haversine(prev.lon, prev.lat, next.lon, next.lat) < 20
            } + debugHistory

    override suspend fun getAllPositions(): List<GpsHistoryEntity> =
        gpsDao.getAll().map(gpsHistoryMapper::from)

    override suspend fun insertPosition(position: GpsHistoryEntity) {
        gpsDao.insert(gpsHistoryMapper.from(position))
    }

    override fun getCompass(): Flow<Float> =
        compass

    override fun observeCompassEnabled(): Flow<Boolean> =
        compassEnabledWatcher.dataFlow

    override suspend fun insertCompass(angle: Float) {
        compass.emit(angle)
    }

    override fun enableCompass() {
        compassEnabledWatcher.notifyUpdated(true)
    }

    override fun disableCompass() {
        compassEnabledWatcher.notifyUpdated(false)
    }
}
