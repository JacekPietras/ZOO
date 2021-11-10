package com.jacekpietras.zoo.map.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.map.R

@Composable
internal fun ToolbarView(
    title: Text,
    onClose: () -> Unit,
) {
    Card(
        shape = RectangleShape,
        backgroundColor = Color.White,
        elevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box {
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
            IconButton(
                modifier = Modifier
                    .then(Modifier.size(48.dp))
                    .align(Alignment.CenterEnd)
                    .padding(12.dp),
                onClick = onClose,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_24),
                    tint = Color.Black,
                    contentDescription = null // decorative element
                )
            }
        }
    }
}
