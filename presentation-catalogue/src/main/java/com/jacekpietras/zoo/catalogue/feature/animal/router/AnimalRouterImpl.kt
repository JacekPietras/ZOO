package com.jacekpietras.zoo.catalogue.feature.animal.router

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.navigation.NavController
import com.jacekpietras.zoo.domain.model.AnimalId

class AnimalRouterImpl(
    private val activityFactory: () -> Activity,
    private val navController: NavController,
) : AnimalRouter {

    override fun navigateToMap(animalId: AnimalId) {
        navController.navigate(Uri.parse("zoo://fragmentMap?animalId=${animalId.id}"))
    }

    override fun navigateToWeb(link: String) {
        val intent = Intent(ACTION_VIEW, Uri.parse(link))
        activityFactory().startActivity(intent)
    }

    override fun navigateToWiki(link: String) {
        val intent = Intent(ACTION_VIEW, Uri.parse(link))
        activityFactory().startActivity(intent)
    }
}
