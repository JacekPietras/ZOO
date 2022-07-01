package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.catalogue.feature.list.model.CatalogueViewState
import com.jacekpietras.zoo.core.ui.ClosableToolbarView

@Composable
internal fun CatalogueView(
    viewState: CatalogueViewState,
    onBackClicked: () -> Unit,
    onCloseClicked: () -> Unit,
    onSearch: (query: String) -> Unit,
    onSearchClicked: () -> Unit,
    onFilterClicked: (AnimalDivision) -> Unit,
    onAnimalClicked: (animalId: String) -> Unit,
) {
    Column {
        if (viewState.isRegionShown) {
            ClosableToolbarView(
                title = viewState.regionName,
                onBack = onBackClicked,
                onClose = onCloseClicked,
            )
        }
        if (viewState.isToolbarVisible) {
            ToolbarWithFilters(
                viewState.filterList,
                viewState.filtersVisible,
                viewState.searchVisible,
                viewState.searchText,
                onSearch = onSearch,
                onSearchClicked = onSearchClicked,
                onFilterClicked = onFilterClicked,
            )
        }
        AnimalList(
            animalList = viewState.animalList,
            onAnimalClicked = onAnimalClicked,
        )
    }
}