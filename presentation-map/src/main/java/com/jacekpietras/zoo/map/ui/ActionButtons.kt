package com.jacekpietras.zoo.map.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.map.R

@Composable
internal fun ActionButtons(
    modifier: Modifier = Modifier,
    onCameraClicked: () -> Unit,
    onLocationClicked: () -> Unit,
) {
    Column(
        Modifier
            .padding(16.dp)
            .then(modifier)
    ) {
        ButtonView(
            modifier = Modifier.padding(bottom = 16.dp),
            icon = R.drawable.ic_camera_24,
            description = R.string.take_photo,
            onClick = onCameraClicked
        )
        ButtonView(
            icon = R.drawable.ic_my_location_24,
            description = R.string.my_location,
            onClick = onLocationClicked
        )
    }
}

@Composable
private fun ButtonView(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes description: Int,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = ZooTheme.colors.surface,
    ) {
        Icon(
            painter = painterResource(icon),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = stringResource(description),
        )
    }
}