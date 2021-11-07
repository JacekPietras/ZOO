package com.jacekpietras.zoo.catalogue.res

import android.content.Context
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.core.extensions.getColorFromAttr

internal val Fragment.colorPrimary: Color
    get() = requireContext().colorPrimary

internal val Context.colorPrimary: Color
    get() = Color(getColorFromAttr(R.attr.colorPrimary))

@Composable
internal fun colorPrimary(): Color =
    Color(LocalContext.current.getColorFromAttr(R.attr.colorPrimary)).takeIf { it.alpha > 0 } ?: MaterialTheme.colors.primary
