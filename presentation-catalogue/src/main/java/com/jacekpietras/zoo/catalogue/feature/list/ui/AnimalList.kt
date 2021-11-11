package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.jacekpietras.zoo.catalogue.BuildConfig
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueListItem

@Composable
internal fun AnimalList(
    animalList: List<CatalogueListItem>,
    onAnimalClicked: (animalId: String) -> Unit,
) {
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
                        .clickable { onAnimalClicked(animal.id) },
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    val painter = rememberImagePainter(
                        data = animal.img ?: "no image",
                        builder = { crossfade(true) }
                    )
                    Image(
                        painter = painter,
                        contentDescription = null, // it's background
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    if (painter.state is ImagePainter.State.Error) {
                        Image(
                            painter = painterResource(R.drawable.pic_banana_leaf_rasterized_2),
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
                            Text(
                                color = Color.Black.copy(alpha = 0.3f),
                                text = animal.regionInZoo.joinToString { it.id }
                            )
                        }
                    }
                }
            }
        }
    }
}
