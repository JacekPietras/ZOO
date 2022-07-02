package com.jacekpietras.zoo.catalogue.feature.animal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.color.MaterialColors
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.model.TextParagraph
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.core.ui.shimmerWhen

@Composable
internal fun AnimalView(
    viewState: AnimalViewState?,
    mapList: List<MapViewLogic.RenderItem<ComposablePaint>>?,
    onWebClicked: () -> Unit,
    onWikiClicked: () -> Unit,
    onNavClicked: () -> Unit,
    onMapSizeChanged: (Int, Int) -> Unit,
    onFavoriteClicked: () -> Unit,
) {
    if (viewState == null) return

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
    ) {
        ImageCarousel(images = viewState.images, contentDescription = viewState.title)
        Spacer(modifier = Modifier.height(16.dp))

        NavigationButtons(viewState, onNavClicked, onFavoriteClicked)

        HeaderView(viewState = viewState)

        if (viewState.feeding != null) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                color = ZooTheme.colors.secondary,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                ) {
                    TitleView(
                        text = RichText(R.string.feeding),
                        color = ZooTheme.colors.onSecondary,
                    )
                    ParagraphView(
                        text = viewState.feeding,
                        color = ZooTheme.colors.onSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        viewState.content.forEach {
            TitleView(text = it.title)
            ParagraphView(text = it.text)
            Spacer(modifier = Modifier.height(16.dp))
        }

        ReadMoreButtonsView(viewState, onWebClicked, onWikiClicked)

        MapView(mapList, onMapSizeChanged)
    }
}

@Composable
private fun NavigationButtons(
    viewState: AnimalViewState,
    onNavClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (viewState.isNavLinkVisible) {
            SimpleButton(
                text = RichText(R.string.nav),
                onClick = onNavClicked,
            )
            SimpleButton(
                text = viewState.favoriteButtonText,
                onClick = onFavoriteClicked,
            )
        }
    }
}

@Composable
private fun MapView(
    mapList: List<MapViewLogic.RenderItem<ComposablePaint>>?,
    onMapSizeChanged: (Int, Int) -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val parentWidth = with(LocalDensity.current) {
        size.width.toDp()
    }

    ComposableMapView(
        Modifier
            .fillMaxWidth()
            .height(parentWidth * 0.6f)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .onSizeChanged { size = it }
            .background(
                color = Color(MaterialColors.getColor(LocalContext.current, R.attr.colorSmallMapBackground, android.graphics.Color.MAGENTA)),
                shape = RoundedCornerShape(8.dp)
            ),
        onSizeChanged = onMapSizeChanged,
        mapList = mapList,
    )
}

@Composable
private fun ReadMoreButtonsView(
    viewState: AnimalViewState,
    onWebClicked: () -> Unit,
    onWikiClicked: () -> Unit,
) {
    if (viewState.isWebLinkVisible || viewState.isWikiLinkVisible) {
        TitleView(text = RichText(R.string.read_more))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (viewState.isWebLinkVisible) {
                SimpleButton(
                    text = RichText(R.string.on_web),
                    onClick = onWebClicked,
                )
            }
            if (viewState.isWikiLinkVisible) {
                SimpleButton(
                    text = RichText(R.string.on_wiki),
                    onClick = onWikiClicked,
                )
            }
        }
    }
}

@Composable
private fun ImageCarousel(
    images: List<String>,
    contentDescription: RichText,
) {
    if (images.isEmpty()) return
    val listState = remember { mutableStateOf(images) }
    val visibilityState = remember { MutableTransitionState(true) }
    val sidePadding = if (images.size > 1) 16.dp else 0.dp

    val state = rememberPagerState()

    AnimatedVisibility(
        visibleState = visibilityState,
        modifier = Modifier.fillMaxSize(),
    ) {
        HorizontalPager(
            count = listState.value.size,
            state = state,
            contentPadding = PaddingValues(start = sidePadding, end = sidePadding, top = sidePadding),
        ) { page ->
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = listState.value.getOrNull(page))
                    .crossfade(true)
                    .build()
            )

            BoxWithConstraints {
                Image(
                    painter = painter,
                    contentDescription = contentDescription.toString(LocalContext.current),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .height(256.dp)
                        .shimmerWhen(
                            width = maxWidth,
                            condition = { painter.state is AsyncImagePainter.State.Loading },
                        ),
                )
            }

            val painterState = painter.state
            if (painterState is AsyncImagePainter.State.Error) {
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
    modifier: Modifier = Modifier,
    viewState: AnimalViewState,
) {
    Row(
        modifier = modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = viewState.title.toString(LocalContext.current),
                modifier = Modifier,
                style = MaterialTheme.typography.h5,
                color = ZooTheme.colors.textPrimaryOnSurface,
            )
            Text(
                text = viewState.subTitle.toString(LocalContext.current),
                modifier = Modifier,
                style = MaterialTheme.typography.caption,
                color = ZooTheme.colors.textPrimaryOnSurface,
            )
        }
        if (viewState.isSeen != null) {
            val painter = if (viewState.isSeen) {
                painterResource(id = R.drawable.ic_visibility_24)
            } else {
                painterResource(id = R.drawable.ic_visibility_off_24)
            }
            val tint = if (viewState.isSeen) {
                ZooTheme.colors.onSurface
            } else {
                ZooTheme.colors.onSurface.copy(alpha = 0.3f)
            }
            val seenText = if (viewState.isSeen) {
                RichText(R.string.seen)
            } else {
                RichText(R.string.not_seen)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = tint,
                )
                Text(
                    text = seenText.toString(LocalContext.current),
                    modifier = Modifier,
                    style = MaterialTheme.typography.caption,
                    color = tint,
                )
            }
        }
    }
}

@Composable
private fun TitleView(
    text: RichText,
    color: Color = ZooTheme.colors.onSurface,
) {
    Text(
        text = text.toString(LocalContext.current),
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.subtitle2,
        color = color,
    )
}

@Composable
private fun ParagraphView(
    text: RichText,
    color: Color = ZooTheme.colors.onSurface,
) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = text.toString(LocalContext.current),
        style = MaterialTheme.typography.body1,
        color = color,
    )
}

@Composable
private fun RowScope.SimpleButton(
    text: RichText,
    onClick: () -> Unit = {},
) {
    SimpleButton(
        modifier = Modifier
            .weight(1f),
        text = text,
        onClick = onClick,
    )
}

@Composable
private fun SimpleButton(
    modifier: Modifier = Modifier,
    text: RichText,
    onClick: () -> Unit = {},
) =
    Button(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minHeight = 56.dp)
            .then(modifier),
        elevation = null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text.toString(LocalContext.current),
            style = MaterialTheme.typography.button,
            textAlign = TextAlign.Center,
        )
    }

@Preview(showBackground = true)
@Composable
private fun AnimalViewPreview() {
    val viewState = AnimalViewState(
        title = RichText.Value("Title"),
        subTitle = RichText.Value("subtitle"),
        content = listOf(
            TextParagraph(
                title = RichText.Value("Paragraph"),
                text = RichText.Value("content"),
            )
        ),
        feeding = RichText.Value("Feeding content"),
        isWikiLinkVisible = true,
        isWebLinkVisible = true,
        isNavLinkVisible = true,
        isSeen = false,
        favoriteButtonText = RichText.Value("Favorite!"),
        images = listOf("https://www.medivet.co.uk/globalassets/assets/puppy--kitten/two-puppies-in-garden.jpg")
    )
    AnimalView(
        viewState = viewState,
        mapList = emptyList(),
        onWebClicked = {},
        onWikiClicked = {},
        onNavClicked = {},
        onFavoriteClicked = {},
        onMapSizeChanged = { _, _ -> },
    )
}