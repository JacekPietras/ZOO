package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.parser.AnimalListWebParser
import com.jacekpietras.zoo.data.parser.AnimalWebParser
import com.jacekpietras.zoo.data.parser.makeStreamFromUrl
import com.jacekpietras.zoo.domain.model.*
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class AnimalRepositoryImpl(
    private val context: Context,
    private val moshi: Moshi,
) : AnimalRepository {

    private val animalFlow = MutableStateFlow<List<AnimalEntity>>(emptyList())

    override suspend fun scrapTestAnimals() {
        withContext(Dispatchers.IO) {
            if (storedAnimals.isEmpty()) {
                storedAnimals = context.resources.openRawResource(R.raw.animals)
                    .bufferedReader()
                    .readText()
                    .parseJsonAnimals()
                animalFlow.value = storedAnimals
            }
        }
    }

    override suspend fun scrapAllAnimals() =
        withContext(Dispatchers.Default) {
            val jsonAdapter = moshi.adapter(AnimalEntity::class.java)

            val webs = storedAnimals.map { it.web }

            AnimalListWebParser(makeStreamFromUrl("https://zoo-krakow.pl/zwierzeta/"))
                .getContent()
                .filterNot { webs.contains(it.www) }
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
                        return@withContext
                    }
                }
        }

    override fun getAnimals(regionId: String): List<AnimalEntity> =
        storedAnimals.filter { it.regionInZoo.contains(regionId) }

    override fun getAnimals(division: Division?): List<AnimalEntity> =
        if (division == null) {
            storedAnimals
        } else {
            storedAnimals.filter { it.division == division }
        }

    override fun observeAnimals(filter: AnimalFilter): Flow<List<AnimalEntity>> =
        animalFlow
            .map {
                it.filter { animal ->
                    when {
                        !animal.containsQuery(filter.query) -> false
                        filter.divisions.isNotEmpty() && !filter.divisions.contains(animal.division) -> false
                        else -> true
                    }
                }
            }

    private fun AnimalEntity.containsQuery(query: String?): Boolean {
        if (query.isNullOrBlank()) return true
        if (this.name.lowercase().contains(query)) return true
        if (this.nameLatin.lowercase().contains(query)) return true
        return false
    }

    override fun getAnimal(animalId: AnimalId): AnimalEntity =
        storedAnimals.firstOrNull { it.id.id == animalId.id }
            ?: throw IllegalArgumentException("Cannot find animal with id: ${animalId.id}")

    private val animalJsonAdapter: JsonAdapter<List<AnimalEntity>>
        get() = moshi.adapter(
            Types.newParameterizedType(MutableList::class.java, AnimalEntity::class.java)
        )

    private fun String.parseJsonAnimals(): List<AnimalEntity> =
        animalJsonAdapter.fromJson(this) ?: emptyList()

    companion object {
        private var storedAnimals: List<AnimalEntity> = emptyList()
    }
}
