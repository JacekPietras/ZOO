package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalFilter
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.feature.animal.repository.AnimalRepository
import com.jacekpietras.zoo.domain.model.RegionId
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AnimalRepositoryImpl(
    private val context: Context,
    private val moshi: Moshi,
) : AnimalRepository {

    private val animalFlow = MutableStateFlow<List<AnimalEntity>>(emptyList())
    private var storedAnimals: List<AnimalEntity> = emptyList()

    override suspend fun loadAnimals() {
        withContext(Dispatchers.IO) {
            synchronized(this) {
                if (storedAnimals.isEmpty()) {
                    storedAnimals = context.resources.openRawResource(R.raw.animals)
                        .bufferedReader()
                        .readText()
                        .parseJsonAnimals()
                        .sortedBy { it.name }
                    animalFlow.value = storedAnimals
                }
            }
        }
    }

    override fun getAnimals(): List<AnimalEntity> =
        storedAnimals

    override fun getAnimals(regionId: RegionId): List<AnimalEntity> =
        storedAnimals.filter { it.regionInZoo.contains(regionId) }

    override fun observeAnimals(filter: AnimalFilter): Flow<List<AnimalEntity>> =
        animalFlow
            .map {
                it.filter { animal ->
                    when {
                        !animal.containsQuery(filter.query) -> false
                        filter.regionId != null && !animal.regionInZoo.contains(filter.regionId!!) -> false
                        filter.division != null && animal.division != filter.division -> false
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
        animalJsonAdapter.fromJson(this)
            ?.map {
                it.copy(
                    occurrence = it.occurrence.correctSpecialCharacters(),
                    environment = it.environment.correctSpecialCharacters(),
                    food = it.food.correctSpecialCharacters(),
                    multiplication = it.multiplication.correctSpecialCharacters(),
                    protectionAndThreats = it.protectionAndThreats.correctSpecialCharacters(),
                    facts = it.facts.correctSpecialCharacters(),
                )
            }
            ?: emptyList()

    private fun String.correctSpecialCharacters(): String =
        replace("&#8222;", "„")
            .replace("&#8221;", "”")
}
