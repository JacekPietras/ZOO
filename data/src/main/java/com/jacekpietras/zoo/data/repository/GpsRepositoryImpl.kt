package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.data.database.dao.GpsDao
import com.jacekpietras.zoo.data.database.mapper.GpsHistoryMapper
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class GpsRepositoryImpl(
    private val gpsDao: GpsDao,
    private val gpsHistoryMapper: GpsHistoryMapper,
) : GpsRepository {

    override fun observeLatestPosition(): Flow<GpsHistoryEntity> =
        gpsDao.getLatest().filterNotNull().onEach {
            checkNotNull(it.timestamp)
            checkNotNull(it.lat)
            checkNotNull(it.lon)
        }.map(gpsHistoryMapper::from)

   override suspend fun insertPosition(position: GpsHistoryEntity) {
        gpsDao.insert(gpsHistoryMapper.from(position))
    }
}