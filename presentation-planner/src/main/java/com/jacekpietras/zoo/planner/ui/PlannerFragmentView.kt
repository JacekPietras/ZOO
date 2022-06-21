package com.jacekpietras.zoo.planner.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.R
import com.jacekpietras.zoo.planner.model.PlannerViewState


@Composable
internal fun PlannerFragmentView(
    viewState: PlannerViewState,
    onRemove: (regionId: String) -> Unit,
    onAddExit: () -> Unit,
) {
    if (viewState.isEmptyViewVisible) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.planner_empty_text),
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2,
                color = ZooTheme.colors.onSurface,
            )
        }
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        items(viewState.list) { item ->
            Card(
                shape = RoundedCornerShape(8.dp),
                backgroundColor = ZooTheme.colors.surface,
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Row {
                    Text(
                        text = item.text.toString(LocalContext.current),
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
                        onClick = { onRemove(item.regionId) },
                    )
                }
            }
        }
    }
    if (viewState.isAddExitVisible) {
        SimpleButton(
            text = Text("Exit"),
            onClick = onAddExit
        )
    }
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
