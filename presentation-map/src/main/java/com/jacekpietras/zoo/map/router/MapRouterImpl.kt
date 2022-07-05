package com.jacekpietras.zoo.map.router

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.navigation.NavController
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

internal class MapRouterImpl(
    private val activityProvider: () -> Activity,
    private val navController: NavController,
) : MapRouter {

    override fun navigateToCamera() {
        activityProvider().startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    override fun navigateToAnimal(animalId: AnimalId) {
        navController.navigate("animal/${animalId.id}")
    }

    override fun navigateToAnimalList(regionId: RegionId) {
        navController.navigate("catalogue/${regionId.id}")
    }

    override fun goBack() {
        navController.navigateUp()
    }
}