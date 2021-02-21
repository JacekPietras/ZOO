package com.jacekpietras.zoo.data.repository

import com.jacekpietras.zoo.data.parser.AnimalListWebParser
import com.jacekpietras.zoo.data.parser.AnimalWebParser
import com.jacekpietras.zoo.data.parser.makeStreamFromUrl
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.divisionFromPolishTag
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AnimalRepositoryImpl(
    private val moshi: Moshi,
) : AnimalRepository {

    override suspend fun scrapTestAnimals() {

    }

    override suspend fun scrapAllAnimals() =
        withContext(Dispatchers.Default) {
            val jsonAdapter = moshi.adapter(AnimalEntity::class.java)

            AnimalListWebParser(makeStreamFromUrl("https://zoo-krakow.pl/zwierzeta/"))
                .getContent()
                .forEach { basic ->
                    try {
                        val parser = AnimalWebParser(makeStreamFromUrl(basic.www))

                        val animal = AnimalEntity(
                            id = AnimalId(parser.getFirstParagraph().title),
                            name = parser.getFirstParagraph().title,
                            nameLatin = parser.getFirstParagraph().content,
                            occurrence = parser.getParagraph("Występowanie").content,
                            environment = parser.getParagraph("Środowisko życia").content,
                            food = parser.getParagraph("Pożywienie").content,
                            multiplication = parser.getParagraph("Rozmnażanie").content,
                            protectionAndThreats = parser.getParagraph("Ochrona i zagrożenia").content,
                            facts = parser.getParagraph("Ciekawostki").content,
                            photos = parser.getPictures().map { it.url },
                            division = divisionFromPolishTag(basic.filter.tag),
                            web = basic.www,
                        )

                        val json = jsonAdapter.toJson(animal)
                        Timber.e("Scrapper $json")
                    } catch (e: Throwable) {
                        Timber.e("Scrapper failed for ${basic.name}")
                    }
                }
        }
}
