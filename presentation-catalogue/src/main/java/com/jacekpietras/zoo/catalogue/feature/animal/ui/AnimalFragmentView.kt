package com.jacekpietras.zoo.catalogue.feature.animal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.model.TextParagraph
import com.jacekpietras.zoo.core.text.Text


@Composable
internal fun AnimalFragmentView(
    viewState: AnimalViewState,
    onWebClicked: () -> Unit,
    onWikiClicked: () -> Unit,
    onNavClicked: () -> Unit,
    onWantToSeeClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
    ) {
        ImageCarousel(
            images = viewState.images,
            contentDescription = viewState.title,
        )
        HeaderView(
            Modifier.padding(bottom = 24.dp, top = 16.dp, start = 16.dp, end = 16.dp),
            viewState.title,
            viewState.subTitle,
            isSeen = viewState.isSeen,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (viewState.isNavLink) {
                SimpleButton(
                    text = Text(R.string.nav),
                    onClick = onNavClicked,
                )
                SimpleButton(
                    text = Text(R.string.want_to_see),
                    onClick = onWantToSeeClicked,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        viewState.content.forEach {
            TitleView(text = it.title)
            ParagraphView(text = it.text)
            Spacer(modifier = Modifier.height(16.dp))
        }

        TitleView(text = Text(R.string.read_more))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (viewState.isWebLink) {
                SimpleButton(
                    text = Text(R.string.on_web),
                    onClick = onWebClicked,
                )
            }
            if (viewState.isWikiLink) {
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
    text: Text,
    subtitle: Text,
    isSeen: Boolean?,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text.toString(LocalContext.current),
                modifier = Modifier,
                style = MaterialTheme.typography.h5,
            )
            Text(
                text = subtitle.toString(LocalContext.current),
                modifier = Modifier,
                style = MaterialTheme.typography.caption,
            )
        }
        if (isSeen != null) {
            val painter = if (isSeen) {
                painterResource(id = R.drawable.ic_visibility_24)
            } else {
                painterResource(id = R.drawable.ic_visibility_off_24)
            }
            val tint = if (isSeen) {
                MaterialTheme.colors.onSurface
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            }
            val seenText = if (isSeen) {
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
        modifier = Modifier.padding(horizontal = 16.dp),
        text = text.toString(LocalContext.current),
        style = MaterialTheme.typography.body1,
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
        isWikiLink = true,
        isWebLink = true,
        isNavLink = true,
        isSeen = false,
        images = listOf("https://www.medivet.co.uk/globalassets/assets/puppy--kitten/two-puppies-in-garden.jpg")
    )
    AnimalFragmentView(
        viewState = viewState,
        onWebClicked = {},
        onWikiClicked = {},
        onNavClicked = {},
        onWantToSeeClicked = {},
    )
}