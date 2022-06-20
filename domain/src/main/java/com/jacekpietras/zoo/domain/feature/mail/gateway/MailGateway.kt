package com.jacekpietras.zoo.domain.feature.mail.gateway

interface MailGateway {

    @Throws(NoMailClientFound::class)
    fun sendMail(address: String, title: String, content: String)

    class NoMailClientFound : Exception()
}