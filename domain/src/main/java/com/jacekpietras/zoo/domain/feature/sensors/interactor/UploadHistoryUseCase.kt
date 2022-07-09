package com.jacekpietras.zoo.domain.feature.sensors.interactor

import com.jacekpietras.zoo.domain.feature.mail.gateway.MailGateway
import com.jacekpietras.zoo.domain.feature.sensors.repository.GpsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadHistoryUseCase(
    private val mailGateway: MailGateway,
    private val gpsRepository: GpsRepository,
) {

    @Throws(UploadFailed::class)
    fun run() {
        try {
            CoroutineScope(Dispatchers.Default).launch {
                val list = gpsRepository.getAllPositions()
                    .joinToString("\n") { "${it.timestamp} ${it.lat} ${it.lon}" }
                mailGateway.sendMail("jacek.marek.pietras+zoo@gmail.com", "Mail from ZOO", list)
            }
        } catch (e: MailGateway.NoMailClientFound) {
            throw UploadFailed()
        }
    }

    class UploadFailed : Exception()
}