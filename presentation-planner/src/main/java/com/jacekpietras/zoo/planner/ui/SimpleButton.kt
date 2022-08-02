package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.RichText

@Composable
internal fun SimpleButton(
    modifier: Modifier = Modifier,
    text: RichText,
    onClick: () -> Unit = {},
) = Button(
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
