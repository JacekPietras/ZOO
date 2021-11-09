package com.jacekpietras.zoo.map.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.Text

@Composable
internal fun ToolbarView(
    title: Text,
) {
    Card(
        shape = RectangleShape,
        backgroundColor = Color.White,
        elevation = 6.dp,
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.toString(LocalContext.current),
                color = Color.Black
            )
        }
    }
}
