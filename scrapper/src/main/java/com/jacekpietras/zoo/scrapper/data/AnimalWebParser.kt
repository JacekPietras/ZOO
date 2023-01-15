package com.jacekpietras.zoo.scrapper.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.*
import java.io.InputStream

internal class AnimalWebParser(inputStream: InputStream) {

    private var result = mutableListOf<WebContent>()

    init {
        Xml.newPullParser()
            .apply { setInput(inputStream, null) }
            .parseAll()
    }

    fun getContent(): List<WebContent> =
        result

    fun getFirstParagraph(): WebContent.Paragraph =
        result
            .filterIsInstance(WebContent.Paragraph::class.java)
            .first()

    fun getParagraph(title: String): WebContent.Paragraph =
        result
            .filterIsInstance(WebContent.Paragraph::class.java)
            .firstOrNull { it.title == title }
            ?: result
                .filterIsInstance(WebContent.Paragraph::class.java)
                .firstOrNull { diffCount(it.title, title) <= 2 }
            ?: throw IllegalStateException("Cannot find title $title in ${getAllTitles()}")

    fun getPictures(): List<WebContent.Image> =
        result.filterIsInstance(WebContent.Image::class.java)

    private fun getAllTitles(): List<String> =
        result
            .filterIsInstance(WebContent.Paragraph::class.java)
            .map { it.title }

    private fun XmlPullParser.parseAll() {
        while (eventType != END_DOCUMENT) {
            when (eventType) {
                START_TAG -> {
                    when {
                        name == "div" && attr("class") == "container" -> parseContainer()
                        name == "head" -> skip()
                        name == "header" -> skip()
                        name == "footer" -> return
                    }
                }
            }
            next()
        }
    }

    private fun XmlPullParser.parseContainer() {
        var deep = 0
        var nextTitle = false
        var lastParagraph: WebContent.Paragraph? = null
        while (true) {
            when (eventType) {
                START_TAG -> {
                    when {
                        attr("class") in listOf(
                            "logotyp col-md-3",
                            "flexslider1",
                            "col-sm-3 border-right top-margin",
                            "col-sm-8 top-margin last-foot",
                            " col-md-9"
                        ) -> skip()
                        attr("class") == "slides" -> parsePictures()
                        name in listOf("h1", "h2", "h3") -> {
                            nextTitle = true
                            deep++
                        }
                        else -> deep++
                    }
                }
                TEXT -> {
                    if (text.trim().isNotEmpty()) {
                        if (nextTitle) {
                            lastParagraph = WebContent.Paragraph(text.trim())
                            result.add(lastParagraph)
                        } else {
                            lastParagraph?.addContent(text.trim())
                        }

                        nextTitle = false
                    }
                }
                END_TAG -> deep--
            }
            if (deep == 0) return
            next()
        }
    }

    private fun XmlPullParser.parsePictures() {
        var deep = 0
        while (true) {
            when (eventType) {
                START_TAG -> {
                    if (name == "img") {
                        result.add(WebContent.Image(attr("src")))
                    }
                    deep++
                }
                END_TAG -> deep--
            }
            if (deep == 0) return
            next()
        }
    }

    sealed class WebContent {

        data class Image(
            val url: String
        ) : WebContent()

        data class Paragraph(
            val title: String,
            var content: String = "",
        ) : WebContent() {

            fun addContent(text: String) {
                if (content.isEmpty()) {
                    content = text
                } else {
                    content += " $text"
                }
            }
        }
    }
}