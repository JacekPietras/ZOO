package com.jacekpietras.zoo.scrapper.data

import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.feature.animal.model.divisionFromPolishTag
import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class WebScrapper(
    private val moshi: Moshi,
    private val animalRepository: AnimalRepository,
) {

    suspend fun scrapAllAnimals(): List<String> {
        val notKnownAnimals = mutableListOf<String>()
        val jsonAdapter = moshi.adapter(AnimalEntity::class.java)

        withContext(Dispatchers.Default) {
            animalRepository.loadAnimals()
            val storedWebs = animalRepository.getAnimals().map(AnimalEntity::web)

            AnimalListWebParser(makeStreamFromUrl("https://zoo-krakow.pl/zwierzeta/"))
                .getContent()
                .filterNot { storedWebs.contains(it.www) }
                .forEach { basic ->
                    notKnownAnimals.add(basic.name)

                    try {
                        val parser = AnimalWebParser(makeStreamFromUrl(basic.www, print = false))

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
                        Timber.i("Scrapper $json")
                    } catch (e: Throwable) {
                        Timber.e(e, "Scrapper failed for ${basic.name} (${basic.www})")
                    }
                }
        }

        if (notKnownAnimals.isEmpty()) {
            notKnownAnimals.add("All Animals are known")
        }

        return notKnownAnimals
    }
}
