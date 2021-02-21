package com.jacekpietras.zoo.data.parser

import android.content.Context
import android.util.Xml
import androidx.annotation.RawRes
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.*

internal class WebParser(context: Context, @RawRes rawRes: Int) {

    private var result: MutableList<WebContent> = mutableListOf()

    init {
        val inputStream = context.resources.openRawResource(rawRes).cleanupHtml()
        val parser = Xml.newPullParser().apply { setInput(inputStream, null) }

        parser.parseAll()
    }

    fun getContent(): List<WebContent> =
        result

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
                END_TAG -> {
                    deep--
                }
            }
            if (deep == 0) return
            next()
        }
    }

    private fun XmlPullParser.skip() {
        var deep = 0
        while (true) {
            when (eventType) {
                START_TAG -> deep++
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
            var content: String? = null,
        ) : WebContent() {

            fun addContent(text: String) {
                if (content == null) {
                    content = text
                } else {
                    content += " $text"
                }
            }
        }
    }
}