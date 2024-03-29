package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jacekpietras.zoo.catalogue.BuildConfig
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueListItem
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.core.ui.shimmerWhen

@Composable
internal fun AnimalList(
    animalList: List<CatalogueListItem>,
    onAnimalClicked: (animalId: String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        items(animalList, key = { it.id }) { animal ->
            Card(
                modifier = Modifier
                    .animateItemPlacement()
                    .height(128.dp)
                    .fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                backgroundColor = ZooTheme.colors.surface,
                elevation = 4.dp,
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onAnimalClicked(animal.id) },
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = animal.img)
                            .crossfade(true)
                            .build()
                    )
                    Image(
                        painter = painter,
                        contentDescription = null, // it's background
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmerWhen(
                                width = maxWidth,
                                condition = { painter.state is AsyncImagePainter.State.Loading },
                            )
                    )
                    if (painter.state is AsyncImagePainter.State.Error) {
                        Image(
                            painter = painterResource(ZooTheme.drawable.bananaLeafRes),
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
