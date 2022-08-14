package com.jacekpietras.zoo.planner.model

import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.planner.R

sealed class SuggestedItem(
    val key: String,
    val text: RichText,
) {

    object Exit : SuggestedItem(key = "exit", text = RichText(R.string.add_exit))
}
