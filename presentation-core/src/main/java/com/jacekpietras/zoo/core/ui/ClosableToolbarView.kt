package com.jacekpietras.zoo.core.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.R
import com.jacekpietras.zoo.core.text.Text

@Composable
fun ClosableToolbarView(
    modifier: Modifier = Modifier,
    title: Text,
    isBackArrowShown: Boolean = true,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Card(
        shape = RectangleShape,
        backgroundColor = Color.White,
        elevation = 6.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ClosableRowView(
                isBackArrowShown = isBackArrowShown,
                onBack = onBack,
                title = title,
                onClose = onClose
            )
            content.invoke(this)
        }
    }
}

@Composable
fun ClosableRowView(
    title: Text,
    isBackArrowShown: Boolean = true,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isBackArrowShown) {
            SideIconView(
                modifier = Modifier.align(Alignment.TopStart),
                iconRes = R.drawable.ic_arrow_back_24,
                contentDescription = R.string.back,
                onClick = onBack,
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = title.toString(LocalContext.current),
                color = Color.Black,
            )
        }
        SideIconView(
            modifier = Modifier.align(Alignment.TopEnd),
            iconRes = R.drawable.ic_close_24,
            contentDescription = R.string.close,
            onClick = onClose,
        )
    }
}

@Composable
private fun SideIconView(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes contentDescription: Int,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .then(Modifier.size(48.dp))
            .padding(12.dp),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconRes),
            tint = Color.Black,
            contentDescription = stringResource(contentDescription)
        )
    }
}
