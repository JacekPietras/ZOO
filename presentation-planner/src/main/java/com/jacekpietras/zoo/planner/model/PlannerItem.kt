package com.jacekpietras.zoo.planner.model

import com.jacekpietras.zoo.core.text.RichText

sealed class PlannerItem(
    val key: String,
) {

    object Header : PlannerItem(key = "header")

    object UserPositionItem : PlannerItem(key = "position")

    data class RegionItem(
        val title: RichText = RichText.Empty,
        val info: RichText = RichText.Empty,
        val regionId: String,
        val isMultiple: Boolean = false,
        val isMutable: Boolean = false,
        val isRemovable: Boolean = true,
        val isFixed: Boolean = false,
        val isSeen: Boolean = false,
    ) : PlannerItem(key = regionId)

    object Footer : PlannerItem(key = "footer")
}
