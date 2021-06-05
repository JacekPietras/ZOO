package com.jacekpietras.zoo.catalogue.feature.animal.router

interface AnimalRouter {

    fun navigateToMap()

    fun navigateToWeb(link: String)

    fun navigateToWiki(link: String)
}