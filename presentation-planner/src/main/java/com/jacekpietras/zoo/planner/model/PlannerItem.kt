package com.jacekpietras.zoo.planner.model

import com.jacekpietras.zoo.core.text.RichText

data class PlannerItem(
    val text: RichText,
    val regionId: String,
    val isMultiple: Boolean = false,
    val isMutable: Boolean = false,
    val isFixed: Boolean = false,
    val isSeen: Boolean = false,
)
