package com.jacekpietras.zoo.catalogue.feature.animal.router

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import com.jacekpietras.zoo.catalogue.feature.animal.ui.AnimalFragmentDirections
import com.jacekpietras.zoo.domain.model.AnimalId

class AnimalRouterImpl(
    private val activityFactory: () -> Activity,
    private val navController: NavController,
) : AnimalRouter {

    override fun navigateToMap(animalId: AnimalId) {
        navController.navigate(AnimalFragmentDirections.navigateToMap(animalId = animalId.id))
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
