package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.core.theme.ZooTheme

@Composable
internal fun ToolbarIcon(
    @DrawableRes id: Int,
    padding: Dp = 0.dp,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {

    val bgPadding = 8.dp
    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            ZooTheme.colors.primary
        } else {
            Color.Transparent
        },
    )
    val bgSize by animateDpAsState(
        targetValue = if (selected) {
            bgPadding
        } else {
            0.dp
        },
    )

    Box {
        Box(
            modifier = Modifier
                .offset(x = bgPadding - bgSize, y = bgPadding - bgSize)
                .clip(RoundedCornerShape(50))
                .background(bgColor)
                .size(24.dp + bgSize * 2)
        )

        IconButton(
            modifier = Modifier
                .padding(padding)
                .size(24.dp + bgPadding * 2),
            onClick = onClick,
        ) {
            val color by animateColorAsState(
                if (selected) {
                    ZooTheme.colors.onPrimary
                } else {
                    ZooTheme.colors.textSecondaryOnSurface
                }
            )

            Icon(
                painter = painterResource(id = id),
                tint = color,
                contentDescription = null // decorative element
            )
        }
    }
}

@Preview(name = "unselected, padding:4dp")
@Composable
private fun UnselectedToolbarIconPreview() {
    ToolbarIcon(
        id = R.drawable.ic_search_24,
        padding = 4.dp,
        selected = false,
    )
}

@Preview(name = "selected, padding:8dp")
@Composable
private fun SelectedToolbarIconPreview() {
    ToolbarIcon(
        id = R.drawable.ic_search_24,
        padding = 8.dp,
        selected = true,
    )
}
