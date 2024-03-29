package com.jacekpietras.zoo.planner.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class PlannerViewState(
    val list: List<PlannerItem> = emptyList(),
    val suggestedItems: List<SuggestedItem> = emptyList(),
    val isEmptyViewVisible: Boolean = false,
    val isShowingUnseeDialog: Boolean = false,
)
