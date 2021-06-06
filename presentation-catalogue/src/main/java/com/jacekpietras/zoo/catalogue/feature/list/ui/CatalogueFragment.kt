package com.jacekpietras.zoo.catalogue.feature.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.imageloading.ImageLoadState
import com.jacekpietras.zoo.catalogue.BuildConfig
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.feature.list.router.CatalogueRouterImpl
import com.jacekpietras.zoo.catalogue.feature.list.viewmodel.CatalogueViewModel
import com.jacekpietras.zoo.core.extensions.getColorFromAttr
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogueFragment : Fragment() {

    private val viewModel by viewModel<CatalogueViewModel>()
    private val router by lazy { CatalogueRouterImpl(findNavController()) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                AnimalList()
            }
        }

    @Composable
    fun AnimalList() {
        val viewState: CatalogueViewState by viewModel.viewState.collectAsState(initial = CatalogueViewState())

        MaterialTheme {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            ) {
                items(viewState.animalList) { animal ->
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        backgroundColor = Color.White,
                        elevation = 4.dp,
                        modifier = Modifier
                            .height(128.dp)
                            .fillMaxSize(),
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(onClick = {
                                    viewModel.onAnimalClicked(
                                        animalId = animal.id,
                                        router = router
                                    )
                                }),
                            contentAlignment = Alignment.BottomEnd,
                        ) {
                            val painter = rememberGlidePainter(
                                animal.img ?: "no image",
                                fadeIn = true,
                            )
                            Image(
                                painter = painter,
                                contentDescription = animal.img,
                                contentScale = ContentScale.Crop,
                            )
                            if (painter.loadState is ImageLoadState.Error) {
                                Image(
                                    painter = rememberGlidePainter(
                                        R.drawable.pic_banana_leaf_rasterized,
                                        fadeIn = true,
                                    ),
                                    contentDescription = null, // decorative element
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(.7f)
                                )
                            }
                            BoxedTextView(text = animal.name)

                            if (BuildConfig.DEBUG) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 32.dp, end = 8.dp),
                            ) {
                                    Text(color = Color(0x55000000), text = animal.regionInZoo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BoxedTextView(text: String) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color = Color(requireContext().getColorFromAttr(android.R.attr.colorPrimary)))
                .padding(vertical = 2.dp, horizontal = 8.dp),
        ) {
            Text(
                color = Color.White,
                text = text,
            )
        }
    }
}
