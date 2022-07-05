package com.jacekpietras.zoo.planner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.viewmodel.PlannerViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun PlannerScreen(navController: NavController) {

    val viewModel = getViewModel<PlannerViewModel>()
    val viewState by viewModel.viewState.collectAsState(initial = PlannerViewState())

    PlannerView(
        viewState = viewState,
        onRemove = viewModel::onRemove,
        onUnlock = viewModel::onUnlock,
        onMove = viewModel::onMove,
        onAddExit = viewModel::onAddExitClicked
    )
}
