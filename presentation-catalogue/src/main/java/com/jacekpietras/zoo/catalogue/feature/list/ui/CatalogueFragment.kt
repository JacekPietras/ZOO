package com.jacekpietras.zoo.catalogue.feature.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.imageloading.ImageLoadState
import com.jacekpietras.zoo.catalogue.BuildConfig
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueListItem
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
                val viewState: CatalogueViewState by viewModel.viewState.collectAsState(initial = CatalogueViewState())

                MaterialTheme {
                    Column {
                        ToolbarWithFilters(viewState.filterList)
                        AnimalList(viewState.animalList)
                    }
                }
            }
        }

    @Composable
    fun ToolbarWithFilters(filterList: List<AnimalDivision>) {
        Card(
            backgroundColor = Color.White,
            elevation = 4.dp,
            modifier = Modifier
                .height(48.dp)
                .fillMaxSize(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                AnimalDivision.values().forEach {
                    ToolbarIcon(id = it.iconRes,
                        selected = filterList.contains(it),
                        onClick = { viewModel.onFilterClicked(it) })
                }
                ToolbarIcon(id = R.drawable.ic_search_24, onClick = {})
            }
        }
    }

    @Composable
    fun ToolbarIcon(@DrawableRes id: Int, selected: Boolean = false, onClick: () -> Unit = {}) {
        IconButton(
            modifier = Modifier
                .then(Modifier.size(24.dp + 8.dp + 8.dp))
                .background(
                    color = if (selected) primaryColor else Color.Transparent,
                    shape = CircleShape,
                ),
            onClick = onClick,
        ) {
            Icon(
                painter = painterResource(id = id),
                tint = if (selected) Color.White else Color.Black,
                contentDescription = null // decorative element
            )
        }
    }

    @Composable
    fun AnimalList(animalList: List<CatalogueListItem>) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            items(animalList) { animal ->
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
                                painter = painterResource(id = R.drawable.pic_banana_leaf_rasterized_2),
                                contentDescription = null, // decorative element
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                        BoxedTextView(text = animal.name)

                        if (BuildConfig.DEBUG) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 32.dp, end = 8.dp),
                            ) {
                                Text(color = Color(0x66000000), text = animal.regionInZoo)
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
                .background(color = primaryColor)
                .padding(vertical = 2.dp, horizontal = 8.dp),
        ) {
            Text(
                color = Color.White,
                text = text,
            )
        }
    }

    private val primaryColor: Color
        get() =
            Color(requireContext().getColorFromAttr(android.R.attr.colorPrimary))
}
