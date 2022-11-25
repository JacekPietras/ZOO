package com.jacekpietras.zoo.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.core.ui.shimmerWhen
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.AnimalDivisionValue
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
                    is MapCarouselItem.Facility -> {
                        FacilityCarouselItem(
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
    modifier: Modifier = Modifier,
    item: MapCarouselItem.Region,
    carouselItemWidth: Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(carouselItemWidth)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .height(carouselItemWidth / 2)
        ) {
            if (item.photoUrlLeftTop != null) AnimalImageView(item.photoUrlLeftTop, carouselItemWidth / 2, item.divisionLeftTop)
            if (item.photoUrlRightTop != null) AnimalImageView(item.photoUrlRightTop, carouselItemWidth / 2, item.divisionRightTop)
        }
        Row(
            modifier = Modifier
                .height(carouselItemWidth / 2)
        ) {
            if (item.photoUrlLeftBottom != null) AnimalImageView(item.photoUrlLeftBottom, carouselItemWidth / 2, item.divisionLeftBottom)
            if (item.photoUrlRightBottom != null) AnimalImageView(item.photoUrlRightBottom, carouselItemWidth / 2, item.divisionRightBottom)
        }
        Text(
            textAlign = TextAlign.Center,
            text = item.name.toString(LocalContext.current),
            color = ZooTheme.colors.textPrimaryOnSurface,
            style = MaterialTheme.typography.caption,
        )
    }
}

@Composable
private fun imageRequest(url: String?) =
    ImageRequest.Builder(LocalContext.current)
        .data(data = url)
        .crossfade(true)
        .build()

@Composable
private fun FacilityCarouselItem(
    modifier: Modifier = Modifier,
    item: MapCarouselItem.Facility,
    carouselItemWidth: Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(carouselItemWidth)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .size(carouselItemWidth),
            painter = painterResource(item.icon),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null,
        )
        Text(
            textAlign = TextAlign.Center,
            text = item.name.toString(LocalContext.current),
            color = ZooTheme.colors.textPrimaryOnSurface,
            style = MaterialTheme.typography.caption,
        )
    }
}

@Composable
private fun AnimalCarouselItem(
    modifier: Modifier = Modifier,
    item: MapCarouselItem.Animal,
    carouselItemWidth: Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(carouselItemWidth)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimalImageView(item.photoUrl, carouselItemWidth, item.division)
        Text(
            textAlign = TextAlign.Center,
            text = item.name.toString(LocalContext.current),
            color = ZooTheme.colors.textPrimaryOnSurface,
            style = MaterialTheme.typography.caption,
        )
    }
}

@Composable
private fun AnimalImageView(url: String?, size: Dp, division: AnimalDivisionValue? = null) {
    val painterState = remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    AsyncImage(
        model = imageRequest(url),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        onLoading = { painterState.value = it },
        onSuccess = { painterState.value = it },
        onError = { painterState.value = it },
        error = painterResource(id = division.getIcon()),
        colorFilter = if (painterState.value is AsyncImagePainter.State.Error) ColorFilter.tint(color = MaterialTheme.colors.onBackground) else null,
        modifier = Modifier
            .shimmerWhen(
                width = size,
                condition = { painterState.value is AsyncImagePainter.State.Loading },
            )
            .placeholderBackgroundWhen(
                condition = { painterState.value is AsyncImagePainter.State.Error },
            )
            .height(size)
            .width(size)
    )
}

private fun AnimalDivisionValue?.getIcon(): Int =
    when (this) {
        AnimalDivisionValue.MAMMAL -> R.drawable.ic_animal_lion_24
        AnimalDivisionValue.BIRD -> R.drawable.ic_animal_bird_24
        AnimalDivisionValue.AMPHIBIAN -> R.drawable.ic_animal_frog_24
        AnimalDivisionValue.REPTILE -> R.drawable.ic_animal_snake_24
        AnimalDivisionValue.FISH -> R.drawable.ic_animal_fish_24
        AnimalDivisionValue.ARTHROPOD -> R.drawable.ic_animal_spider_24
        AnimalDivisionValue.MOLLUSK -> R.drawable.ic_animal_snail_24
        null -> R.drawable.ic_close_24
    }

@Composable
fun Modifier.placeholderBackgroundWhen(condition: () -> Boolean): Modifier =
    if (condition()) {
        background(color = ZooTheme.colors.onSurface.copy(alpha = 0.1f))
            .alpha(0.1f)
    } else {
        this
    }