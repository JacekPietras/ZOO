package com.jacekpietras.zoo.catalogue.feature.animal.router

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController

class AnimalRouterImpl(
    private val activityFactory: () -> Activity,
    private val navController: NavController,
) : AnimalRouter {

    override fun navigateToMap() {
        TODO("Not yet implemented")
    }

    override fun navigateToWeb(link: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
        }
        activityFactory().startActivity(intent)
    }

    override fun navigateToWiki(link: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
        }
        activityFactory().startActivity(intent)
    }
}