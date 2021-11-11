package com.jacekpietras.zoo.catalogue.feature.animal.mapper

import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.core.text.Text

internal class AnimalMapper {

    fun from(state: AnimalState): AnimalViewState =
        AnimalViewState(
            title = Text(state.animal.name),
            subTitle = Text(state.animal.nameLatin),
            content = with(state.animal) {
                listOf(
                    paragraph(Text(R.string.occurence), occurrence),
                    paragraph(Text(R.string.environment), environment),
                    paragraph(Text(R.string.food), food),
                    paragraph(Text(R.string.multiplication), multiplication),
                    paragraph(Text(R.string.protection_and_threats), protectionAndThreats),
                    paragraph(Text(R.string.facts), facts),
                )
                    .filter { it !is Text.Empty }
                    .let { Text.Listing(it, Text("\n\n")) }
            },
            isWikiLink = state.animal.wiki.isNotBlank(),
            isWebLink = state.animal.web.isNotBlank(),
            navLinks = state.animal.regionInZoo.map { it.id },
            images = state.animal.photos,
        )

    private fun paragraph(title: Text, content: String): Text =
        if (content.isNotBlank()) {
            title + "\n" + Text(content)
        } else {
            Text.Empty
        }
}