package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AnimalFragment : Fragment() {

    private val args: AnimalFragmentArgs by navArgs()
    private val viewModel by viewModel<AnimalViewModel> {
        parametersOf(args.animalId)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                val name: AnimalViewState by viewModel.viewState.observeAsState(AnimalViewState("empty"))
                Text(
                    text = name.title,
                )
            }
        }
}
