package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.data.database.dao.GpsDao
import com.jacekpietras.zoo.data.database.mapper.GpsHistoryMapper
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

internal class GpsRepositoryImpl(
    private val gpsDao: GpsDao,
    private val gpsHistoryMapper: GpsHistoryMapper,
) : GpsRepository {

    override fun observeLatestPosition(): Flow<GpsHistoryEntity> =
        gpsDao.getLatest().filterNotNull().map(gpsHistoryMapper::from)

    override suspend fun getAllPositions(): List<GpsHistoryEntity> =
        gpsDao.getAll().map(gpsHistoryMapper::from)

    override suspend fun insertPosition(position: GpsHistoryEntity) {
        gpsDao.insert(gpsHistoryMapper.from(position))
    }
}