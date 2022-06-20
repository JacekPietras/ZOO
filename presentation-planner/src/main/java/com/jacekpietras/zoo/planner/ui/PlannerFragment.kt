package com.jacekpietras.zoo.planner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.viewmodel.PlannerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlannerFragment : Fragment() {

    private val viewModel by viewModel<PlannerViewModel>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            val viewState by viewModel.viewState.observeAsState(initial = PlannerViewState())

            ZooTheme {
                PlannerFragmentView(
                    viewState = viewState,
                    onRemove = viewModel::onRemove,
                )
            }
        }
    }
}