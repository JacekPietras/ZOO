package com.jacekpietras.zoo.planner.extensions

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier

internal fun Modifier.statusBarsPaddingWhen(condition: () -> Boolean): Modifier =
    if (condition()) {
        statusBarsPadding()
    } else {
        this
    }
