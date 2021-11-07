package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.rememberImagePainter
import com.google.android.material.composethemeadapter.MdcTheme
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

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            val viewState by viewModel.viewState.observeAsState(AnimalViewState())

            with(viewState) {
                MdcTheme {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        val painter = rememberImagePainter(
                            data = images.firstOrNull() ?: "no image",
                            builder = { crossfade(true) }
                        )
                        Image(
                            painter = painter,
                            contentDescription = title.toString(LocalContext.current),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .height(256.dp)
                        )
                        SimpleText(title)
                        SimpleText(subTitle)
                        SimpleText(content)
                        if (isWikiLink) {
                            SimpleButton(
                                text = Text(R.string.wiki),
                                onClick = { viewModel.onWikiClicked(router) },
                            )
                        }
                        if (isWebLink) {
                            SimpleButton(
                                text = Text(R.string.web),
                                onClick = { viewModel.onWebClicked(router) },
                            )
                        }

                        navLinks.forEach {
                            SimpleButton(
                                text = Text(R.string.nav) + " " + it,
                                onClick = { viewModel.onNavClicked(router, it) },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SimpleText(
        text: Text,
    ) {
        Text(
            text = text.toString(LocalContext.current),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }

    @Composable
    private fun SimpleButton(
        text: Text,
        onClick: () -> Unit = {},
    ) {
        SimpleButton(
            text = text,
            onClick = onClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }

    @Composable
    private fun SimpleButton(
        modifier: Modifier = Modifier,
        text: Text,
        onClick: () -> Unit = {},
    ) =
        Button(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(text = text.toString(LocalContext.current))
        }
}
