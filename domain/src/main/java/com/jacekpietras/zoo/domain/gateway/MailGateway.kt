package com.jacekpietras.zoo.domain.gateway

interface MailGateway {

    @Throws(NoMailClientFound::class)
    fun sendMail(address: String, title: String, content: String)

    class NoMailClientFound : Exception()
}