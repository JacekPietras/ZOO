package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.R

@Composable
internal fun EmptyView() {
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
