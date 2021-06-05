package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouterImpl
import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import com.jacekpietras.zoo.core.text.Text
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AnimalFragment : Fragment() {

    private val args: AnimalFragmentArgs by navArgs()
    private val viewModel by viewModel<AnimalViewModel> {
        parametersOf(args.animalId)
    }
    private val router by lazy { AnimalRouterImpl(::requireActivity, findNavController()) }

    // todo there is no char sequence
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                val viewState by viewModel.viewState.observeAsState(AnimalViewState())
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = viewState.title.toString(context),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    Text(
                        text = viewState.subTitle.toString(context),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    Text(
                        text = viewState.content.toString(context),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    if (viewState.isWikiLink) {
                        SimpleButton(
                            text = Text(R.string.wiki),
                            onClick = { viewModel.onWikiClicked(router) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    if (viewState.isWebLink) {
                        SimpleButton(
                            text = Text(R.string.web),
                            onClick = { viewModel.onWebClicked(router) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    if (viewState.isNavLink) {
                        SimpleButton(
                            text = Text(R.string.nav),
                            onClick = { viewModel.onNavClicked(router) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

    @Composable
    fun SimpleButton(
        modifier: Modifier = Modifier,
        text: Text,
        onClick: () -> Unit = {},
    ) =
        Button(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(text = text.toString(requireContext()))
        }
}
