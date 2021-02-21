package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.parser.AnimalListWebParser
import com.jacekpietras.zoo.data.parser.AnimalWebParser
import com.jacekpietras.zoo.data.parser.makeStreamFromUrl
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.divisionFromPolishTag
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AnimalRepositoryImpl(
    private val context: Context,
    private val moshi: Moshi,
) : AnimalRepository {

    private var storedAnimals: List<AnimalEntity> = emptyList()

    override suspend fun scrapTestAnimals() {
        withContext(Dispatchers.IO) {

            val jsonText = context.resources.openRawResource(R.raw.animals)
                .bufferedReader()
                .readText()
            val type =
                Types.newParameterizedType(MutableList::class.java, AnimalEntity::class.java)
            val adapter: JsonAdapter<List<AnimalEntity>> = moshi.adapter(type)
            runCatching { adapter.fromJson(jsonText) }
                .onSuccess { storedAnimals = it ?: emptyList() }

            Timber.v("Scrapper ${storedAnimals.size} animals preloaded")
        }
    }

    override suspend fun scrapAllAnimals() =
        withContext(Dispatchers.Default) {
            val jsonAdapter = moshi.adapter(AnimalEntity::class.java)

            val webs = storedAnimals.map { it.web }

            AnimalListWebParser(makeStreamFromUrl("https://zoo-krakow.pl/zwierzeta/"))
                .getContent()
                .filter { !webs.contains(it.www) }
                .forEach { basic ->
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
                        Timber.e("Scrapper $json")
                    } catch (e: Throwable) {
                        Timber.e(e, "Scrapper failed for ${basic.name}")
                        //todo remove return
                        return@withContext
                    }
                }
        }
}
