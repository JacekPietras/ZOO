package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.planner.R

@Composable
internal fun SuggestionButton(
    text: RichText,
    onClick: () -> Unit = {},
) = Button(
    onClick = onClick,
    elevation = null,
    shape = RoundedCornerShape(8.dp)
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_add_24),
        tint = MaterialTheme.colors.onPrimary,
        contentDescription = null // decorative element
    )
    Text(
        modifier = Modifier.padding(horizontal = 4.dp),
        text = text.toString(LocalContext.current),
        style = MaterialTheme.typography.button,
        textAlign = TextAlign.Center,
    )
}
