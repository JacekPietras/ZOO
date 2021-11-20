package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
internal fun RowScope.SearchView(searchText: String, onSearch: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .padding(8.dp)
            .background(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
            )
    ) {
        var value by rememberSaveable { mutableStateOf(searchText) }
        val focusRequester = FocusRequester()
        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .wrapContentSize(Alignment.CenterStart),
            value = value,
            onValueChange = {
                value = it
                onSearch(it)
            },
            textStyle = TextStyle.Default.copy(
                color = MaterialTheme.colors.onSurface
            ),
            maxLines = 1,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colors.onSurface)
        )
        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    }
}
