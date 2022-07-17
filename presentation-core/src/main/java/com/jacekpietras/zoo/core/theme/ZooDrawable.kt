@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.jacekpietras.zoo.core.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.jacekpietras.zoo.core.R

@Immutable
class ZooDrawable(
    private val large: Boolean = false,
    private val nightTheme: Boolean = false,
) {

    @DrawableRes
    val bananaLeafRes: Int = R.drawable.pic_banana_leaf_day onNight R.drawable.pic_banana_leaf_night

    private infix fun <T> T.onNight(right: T): T = if (nightTheme) right else this
}