package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
                color = Color.Black.copy(alpha = 0.1f)
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
            maxLines = 1,
            singleLine = true,
        )
        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    }
}
