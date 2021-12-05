package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.color.MaterialColors
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.model.TextParagraph
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.core.theme.ZooTheme


@Composable
internal fun AnimalFragmentView(
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
                        text = Text(R.string.feeding),
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
                text = Text(R.string.nav),
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
        TitleView(text = Text(R.string.read_more))
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
                    text = Text(R.string.on_web),
                    onClick = onWebClicked,
                )
            }
            if (viewState.isWikiLinkVisible) {
                SimpleButton(
                    text = Text(R.string.on_wiki),
                    onClick = onWikiClicked,
                )
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
                Text(R.string.seen)
            } else {
                Text(R.string.not_seen)
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
    text: Text,
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
    text: Text,
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
    text: Text,
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
    text: Text,
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

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
private fun AnimalFragmentPreview() {
    val viewState = AnimalViewState(
        title = Text.Value("Title"),
        subTitle = Text.Value("subtitle"),
        content = listOf(
            TextParagraph(
                title = Text.Value("Paragraph"),
                text = Text.Value("content"),
            )
        ),
        feeding = Text.Value("Feeding content"),
        isWikiLinkVisible = true,
        isWebLinkVisible = true,
        isNavLinkVisible = true,
        isSeen = false,
        favoriteButtonText = Text.Value("Favorite!"),
        images = listOf("https://www.medivet.co.uk/globalassets/assets/puppy--kitten/two-puppies-in-garden.jpg")
    )
    AnimalFragmentView(
        viewState = viewState,
        mapList = emptyList(),
        onWebClicked = {},
        onWikiClicked = {},
        onNavClicked = {},
        onFavoriteClicked = {},
        onMapSizeChanged = { _, _ -> },
    )
}