package com.jacekpietras.zoo.planner.ui

import androidx.compose.runtime.Composable
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.model.SuggestedItem

@Composable
internal fun PlannerView(
    viewState: PlannerViewState,
    onRemove: (regionId: String) -> Unit,
    onUnlock: (regionId: String) -> Unit,
    onUnsee: (regionId: String) -> Unit,
    onUnseen: () -> Unit,
    onUnseeDiscarded: () -> Unit,
    onMove: (from: String, to: String) -> Unit,
    onSuggestedItemClicked: (suggestedItem: SuggestedItem) -> Unit,
) {
    if (viewState.isEmptyViewVisible) {
        EmptyView()
    }

    PlannerListView(
        viewState = viewState,
        onMove = onMove,
        onRemove = onRemove,
        onUnlock = onUnlock,
        onUnsee = onUnsee,
        onSuggestedItemClicked = onSuggestedItemClicked,
    )

    TopGradientView()

    if (viewState.isShowingUnseeDialog) {
        MeetAgainDialog(onUnseeDiscarded, onUnseen)
    }
}
