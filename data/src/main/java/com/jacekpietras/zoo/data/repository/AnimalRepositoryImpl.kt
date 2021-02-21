package com.jacekpietras.zoo.data.repository

import android.content.Context
import com.jacekpietras.zoo.data.R
import com.jacekpietras.zoo.data.parser.WebParser
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.repository.AnimalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AnimalRepositoryImpl(
    private val context: Context,
) : AnimalRepository {

    override suspend fun getMyszojelen(): AnimalEntity =
        withContext(Dispatchers.Default) {
            val parser = WebParser(context, R.raw.myszojelen)
            Timber.e("Scrapper ------------------------------")
            parser.getContent().forEach { Timber.e("Scrapper $it") }

            return@withContext AnimalEntity(
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
            )
        }
}
