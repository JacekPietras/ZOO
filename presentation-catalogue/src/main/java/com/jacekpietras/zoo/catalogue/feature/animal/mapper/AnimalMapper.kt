package com.jacekpietras.zoo.catalogue.feature.animal.mapper

import androidx.annotation.StringRes
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.model.TextParagraph
import com.jacekpietras.zoo.core.text.Text

internal class AnimalMapper {

    fun from(state: AnimalState): AnimalViewState =
        AnimalViewState(
            title = Text(state.animal.name),
            subTitle = Text(state.animal.nameLatin),
            content = with(state.animal) {
                listOfNotNull(
                    paragraph(R.string.occurrence, occurrence),
                    paragraph(R.string.environment, environment),
                    paragraph(R.string.food, food),
                    paragraph(R.string.multiplication, multiplication),
                    paragraph(R.string.protection_and_threats, protectionAndThreats),
                    paragraph(R.string.facts, facts),
                )
            },
            isWikiLink = state.animal.wiki.isNotBlank(),
            isWebLink = state.animal.web.isNotBlank(),
            isNavLink = state.animal.regionInZoo.isNotEmpty(),
            images = state.animal.photos,
        )

    private fun paragraph(@StringRes title: Int, content: String): TextParagraph? =
        if (content.isNotBlank()) {
            TextParagraph(
                title = Text(title),
                text = Text(content),
            )
        } else {
            null
        }
}