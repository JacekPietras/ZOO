package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.parser.WebParser
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AnimalRepositoryImpl(
    private val context: Context,
) : AnimalRepository {

    override suspend fun getMyszojelen(): AnimalEntity {
        withContext(Dispatchers.Default) {
            val parser = WebParser(context, R.raw.myszojelen).getContent()
            Timber.e("Scrapper $parser")
        }
        return AnimalEntity()
    }
}
