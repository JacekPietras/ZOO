package com.jacekpietras.zoo.planner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.planner.model.PlannerViewState

@Composable
internal fun PlannerView(
    viewState: PlannerViewState,
    onRemove: (regionId: String) -> Unit,
    onUnlock: (regionId: String) -> Unit,
    onUnsee: (regionId: String) -> Unit,
    onUnseen: () -> Unit,
    onUnseeDiscarded: () -> Unit,
    onMove: (from: String, to: String) -> Unit,
    onAddExit: () -> Unit,
) {
    if (viewState.isEmptyViewVisible) {
        EmptyView()
    }

    Column {
        PlannerListView(
            modifier = Modifier.weight(1f),
            viewState = viewState,
            onMove = onMove,
            onRemove = onRemove,
            onUnlock = onUnlock,
            onUnsee = onUnsee,
        )
        if (viewState.isAddExitVisible) {
            SimpleButton(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.End),
                text = RichText("Add Exit"),
                onClick = onAddExit
            )
        }
    }

    TopGradientView()

    if (viewState.isShowingUnseeDialog) {
        MeetAgainDialog(onUnseeDiscarded, onUnseen)
    }
}
