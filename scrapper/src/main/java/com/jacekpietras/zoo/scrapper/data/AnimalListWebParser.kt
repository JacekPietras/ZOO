package com.jacekpietras.zoo.scrapper.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.*
import java.io.InputStream

internal class AnimalListWebParser(inputStream: InputStream) {

    private var result = mutableListOf<Animal>()
    private var filters = mutableListOf<Filter>()

    init {
        Xml.newPullParser()
            .apply { setInput(inputStream, null) }
            .parseAll()
    }

    fun getContent(): List<Animal> = result

    private fun XmlPullParser.parseAll() {
        while (eventType != END_DOCUMENT) {
            when (eventType) {
                START_TAG -> {
                    when {
                        name == "div" && attr("id") == "filters" -> parseFilters()
                        name == "div" && attr("id") == "portfolio-items-wrap" -> parseAnimals()
                        name == "head" -> skip()
                        name == "header" -> skip()
                        name == "footer" -> return
                    }
                }
            }
            next()
        }
    }

    private fun XmlPullParser.parseFilters() {
        var deep = 0
        var dataFilter = ""
        while (true) {
            when (eventType) {
                START_TAG -> {
                    attr("data-filter")
                        .takeIf { it.isNotBlank() }
                        ?.also { dataFilter = it.replace(".", "") }
                    deep++
                }
                END_TAG -> deep--
                TEXT -> if (text.isNotBlank()) filters.add(Filter(text.trim(), dataFilter))
            }
            if (deep == 0) return
            next()
        }
    }

    private fun XmlPullParser.parseAnimals() {
        var deep = 0
        var lastAnimal: Animal? = null
        while (true) {
            when (eventType) {
                START_TAG -> {
                    when {
                        attr("class").startsWith("portfolio-item") -> {
                            val filterTag = attr("class").split(" ").last()
                            val filter = filters.firstOrNull { it.tag == filterTag }
                            if (filter != null) {
                                lastAnimal = Animal(filter = filter)
                                result.add(lastAnimal)
                            }
                        }
                        name == "a" && attr("target") == "_blank" -> lastAnimal?.www = attr("href")
                        name == "img" -> lastAnimal?.photo = attr("src")
                    }
                    deep++
                }
                END_TAG -> deep--
                TEXT -> {
                    if (text.isNotBlank()) {
                        lastAnimal?.name = text.trim()
                    }
                }
            }
            if (deep == 0) return
            next()
        }
    }

    data class Filter(
        val division: String,
        val tag: String,
    )

    data class Animal(
        var name: String = "",
        val filter: Filter,
        var photo: String = "",
        var www: String = "",
    )
}