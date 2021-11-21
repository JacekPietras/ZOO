package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
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
                        ImageCarousel(
                            images = images,
                            contentDescription = title,
                        )
                        HeaderView(title, subTitle)
                        content.forEach {
                            TitleView(text = it.title)
                            ParagraphView(text = it.text)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
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
                        if (isNavLink) {
                            SimpleButton(
                                text = Text(R.string.nav),
                                onClick = { viewModel.onNavClicked(router) },
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun ImageCarousel(
        images: List<String>,
        contentDescription: Text,
    ) {
        if (images.isEmpty()) return
        val listState = remember { mutableStateOf(images) }
        val visibilityState = remember { MutableTransitionState(true) }

        val state = rememberPagerState()

        AnimatedVisibility(
            visibleState = visibilityState,
            modifier = Modifier.fillMaxSize(),
        ) {
            HorizontalPager(
                count = listState.value.size,
                state = state,
            ) { page ->
                val painter = rememberImagePainter(
                    data = listState.value.getOrNull(page) ?: "no image",
                    builder = { crossfade(true) },
                )

                Image(
                    painter = painter,
                    contentDescription = contentDescription.toString(LocalContext.current),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(256.dp)
                )

                val painterState = painter.state
                if (painterState is ImagePainter.State.Error) {
                    val afterRemoving = listState.value - (painterState.result.request.data as String)

                    if (afterRemoving.isEmpty()) {
                        visibilityState.targetState = false
                    } else {
                        listState.value = afterRemoving
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderView(
        text: Text,
        subtitle: Text,
    ) {
        Text(
            text = text.toString(LocalContext.current),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.h5,
        )
        Text(
            text = subtitle.toString(LocalContext.current),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.caption,
        )
        Spacer(Modifier.height(24.dp))
    }

    @Composable
    private fun TitleView(
        text: Text,
    ) {
        Text(
            text = text.toString(LocalContext.current),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.subtitle2,
        )
    }

    @Composable
    private fun ParagraphView(
        text: Text,
    ) {
        Text(
            text = text.toString(LocalContext.current),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.body1,
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
            modifier = Modifier.padding(horizontal = 16.dp),
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
