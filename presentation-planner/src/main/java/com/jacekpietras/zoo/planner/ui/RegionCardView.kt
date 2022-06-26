package com.jacekpietras.zoo.planner.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.R

@Composable
internal fun RegionCardView(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    text: String,
    onRemove: () -> Unit = {},
) {
    val elevationState by animateDpAsState(elevation)

    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = ZooTheme.colors.surface,
        elevation = elevationState,
        modifier = modifier
            .fillMaxSize(),
    ) {
        Row {
            Text(
                text = text,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                style = MaterialTheme.typography.subtitle2,
                color = ZooTheme.colors.onSurface,
            )

            SideIconView(
                modifier = Modifier.align(Alignment.CenterVertically),
                iconRes = R.drawable.ic_close_24,
                contentDescription = R.string.remove_from_plan,
                onClick = onRemove,
            )
        }
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
            .then(Modifier.size(48.dp)),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconRes),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = stringResource(contentDescription)
        )
    }
}
