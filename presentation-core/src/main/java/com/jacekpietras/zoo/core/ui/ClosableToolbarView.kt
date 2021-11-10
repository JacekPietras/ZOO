package com.jacekpietras.zoo.core.ui

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
    title: Text,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Card(
        shape = RectangleShape,
        backgroundColor = Color.White,
        elevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box {
            IconButton(
                modifier = Modifier
                    .then(Modifier.size(48.dp))
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                onClick = onBack,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back_24),
                    tint = Color.Black,
                    contentDescription = stringResource(R.string.back)
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
            IconButton(
                modifier = Modifier
                    .then(Modifier.size(48.dp))
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                onClick = onClose,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_24),
                    tint = Color.Black,
                    contentDescription = stringResource(R.string.close)
                )
            }
        }
    }
}
