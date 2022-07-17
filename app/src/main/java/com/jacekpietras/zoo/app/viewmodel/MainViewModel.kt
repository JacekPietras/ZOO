package com.jacekpietras.zoo.app.viewmodel

import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.app.model.MainState
import com.jacekpietras.zoo.app.model.MainViewState
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.domain.interactor.ObserveSuggestedThemeTypeUseCase
import com.jacekpietras.zoo.domain.model.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MainViewModel(
    observeSuggestedThemeTypeUseCase: ObserveSuggestedThemeTypeUseCase,
) : ViewModel() {

    private val state = observeSuggestedThemeTypeUseCase.run()
        .map { (theme, _) -> MainState(themeType = theme) }
        .flowOnBackground()
    val viewState: Flow<MainViewState> = state
        .map { MainViewState(isNightModeSuggested = it.themeType == ThemeType.NIGHT) }
}