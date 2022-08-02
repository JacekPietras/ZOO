package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.planner.R

@Composable
internal fun MeetAgainDialog(onUnseeDiscarded: () -> Unit, onUnseen: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onUnseeDiscarded() },
        title = {
            Text(text = stringResource(R.string.unsee_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.unsee_dialog_content))
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            ) {
                Button(
                    onClick = { onUnseeDiscarded() }
                ) {
                    Text(text = stringResource(R.string.unsee_dialog_button_decline))
                }
                Button(
                    onClick = { onUnseen() }
                ) {
                    Text(text = stringResource(R.string.unsee_dialog_button_confirm))
                }
            }
        }
    )
}
