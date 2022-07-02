package com.jacekpietras.zoo.catalogue.res

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.core.extensions.getColorFromAttr

@Composable
internal fun colorPrimary(): Color =
    Color(LocalContext.current.getColorFromAttr(R.attr.colorPrimary)).takeIf { it.alpha > 0 } ?: MaterialTheme.colors.primary
