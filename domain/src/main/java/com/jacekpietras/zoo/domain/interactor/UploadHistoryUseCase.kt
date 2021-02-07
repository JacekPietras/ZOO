package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.gateway.MailGateway

class UploadHistoryUseCase(
    private val mailGateway: MailGateway,
) {

    @Throws(UploadFailed::class)
    operator fun invoke() {
        try {
            mailGateway.sendMail("jacek.marek.pietras+zoo@gmail.com", "Mail from ZOO", "test mail")
        } catch (e: MailGateway.NoMailClientFound) {
            throw UploadFailed()
        }
    }

    class UploadFailed : Exception()
}