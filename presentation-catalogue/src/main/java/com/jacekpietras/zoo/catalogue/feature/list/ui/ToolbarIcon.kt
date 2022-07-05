package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.catalogue.R

@Composable
internal fun ToolbarIcon(
    @DrawableRes id: Int,
    padding: Dp = 0.dp,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    IconButton(
        modifier = Modifier
            .padding(padding)
            .then(Modifier.size(24.dp + 8.dp + 8.dp))
            .background(
                color = if (selected) MaterialTheme.colors.primary else Color.Transparent,
                shape = CircleShape,
            ),
        onClick = onClick,
    ) {
        val color by animateColorAsState(
            if (selected) {
                Color.White
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            }
        )

        Icon(
            painter = painterResource(id = id),
            tint = color,
            contentDescription = null // decorative element
        )
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
