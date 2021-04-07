package com.jacekpietras.zoo.catalogue.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.accompanist.glide.GlideImage
import com.jacekpietras.zoo.catalogue.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.viewmodel.CatalogueViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogueFragment : Fragment() {

    private val viewModel by viewModel<CatalogueViewModel>()

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
                        backgroundColor = Color.LightGray,
                        elevation = 4.dp,
                        modifier = Modifier
                            .height(96.dp)
                            .fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(onClick = { }),
                        ) {
                            GlideImage(
                                data = animal.img ?: "no image",
                                contentDescription = animal.name,
                                fadeIn = true,
                                contentScale = ContentScale.Crop,
                            )
                            Text(
                                text = "${animal.name} - ${animal.regionInZoo}",
                                modifier = Modifier
                                    .align(alignment = Alignment.BottomEnd)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}
