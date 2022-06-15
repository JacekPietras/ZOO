package com.jacekpietras.zoo.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberImagePainter
import com.jacekpietras.zoo.core.ui.shimmerWhen
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.model.MapCarouselItem

@Composable
fun ImageCarouselView(
    mapCarouselItems: List<MapCarouselItem>,
    itemWidth: Dp,
    onAnimalClicked: (AnimalId) -> Unit,
    onRegionClicked: (RegionId) -> Unit,
) {
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(mapCarouselItems.isNotEmpty()) }
            .apply { targetState = mapCarouselItems.isNotEmpty() },
        modifier = Modifier.fillMaxWidth(),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        LazyRow(
            modifier = Modifier
                .defaultMinSize(minHeight = itemWidth + 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            items(mapCarouselItems) { carouselItem ->
                when (carouselItem) {
                    is MapCarouselItem.Animal -> {
                        AnimalCarouselItem(
                            item = carouselItem,
                            carouselItemWidth = itemWidth,
                            onClick = { onAnimalClicked(carouselItem.id) },
                        )
                    }
                    is MapCarouselItem.Region -> {
                        RegionCarouselItem(
                            item = carouselItem,
                            carouselItemWidth = itemWidth,
                            onClick = { onRegionClicked(carouselItem.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionCarouselItem(
    item: MapCarouselItem.Region,
    carouselItemWidth: Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(carouselItemWidth)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row {
            AnimalImageView(item.photoUrlLeftTop, carouselItemWidth / 2)
            AnimalImageView(item.photoUrlRightTop, carouselItemWidth / 2)
        }
        Row {
            AnimalImageView(item.photoUrlLeftBottom, carouselItemWidth / 2)
            AnimalImageView(item.photoUrlRightBottom, carouselItemWidth / 2)
        }
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier,
            text = item.name.toString(LocalContext.current),
            color = MaterialTheme.colors.onSurface,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun imagePainter(url: String?) = rememberImagePainter(
    data = url ?: "no image",
    builder = { crossfade(true) }
)

@Composable
private fun AnimalCarouselItem(
    item: MapCarouselItem.Animal,
    carouselItemWidth: Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(carouselItemWidth)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimalImageView(item.photoUrl, carouselItemWidth)
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier,
            text = item.name.toString(LocalContext.current),
            color = MaterialTheme.colors.onSurface,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun AnimalImageView(url: String?, size: Dp) {
    val painter = imagePainter(url)
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .shimmerWhen(
                width = size,
                condition = { painter.state is AsyncImagePainter.State.Loading },
            )
            .height(size)
            .width(size)
    )
}
