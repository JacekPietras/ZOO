package com.jacekpietras.zoo.data.parser

import android.content.res.XmlResourceParser
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal fun XmlResourceParser.attr(name: String): String =
    getAttributeValue(null, name) ?: ""

internal fun XmlResourceParser.attrD(name: String): Double =
    getAttributeValue(null, name).toDouble()

internal fun XmlPullParser.attr(name: String): String =
    getAttributeValue(null, name) ?: ""

internal fun XmlPullParser.attrD(name: String): Double =
    getAttributeValue(null, name).toDouble()

internal fun countMatches(text: String, template: String): Int {
    var cnt = 0
    var pos = 0
    while (true) {
        pos = text.indexOf(template, pos)
        if (pos != -1) {
            cnt++
            pos++
        } else {
            return cnt
        }
    }
}

internal fun String.removeLast(toRemove: String, times: Int = 1): String {
    if (times <= 0) return this
    var result = this
    for (i in 1..times) {
        val pos = result.lastIndexOf(toRemove)
        if (pos == -1) {
            if (times > 1) {
                throw IllegalStateException(
                    "removeLast: \"$toRemove\" is not in \"$this\" $times times"
                )
            } else {
                throw IllegalStateException(
                    "removeLast: \"$toRemove\" is not in \"$this\""
                )
            }
        }
        result = result.substring(0, pos) + result.substring(pos + toRemove.length, result.length)
    }
    return result
}

internal fun InputStream.cleanupHtml(): InputStream {
    val outputStream = ByteArrayOutputStream()
    var insideScript = false
    var insideImg = false
    var openedDiv = 0
    var closedDiv = 0

    bufferedReader().forEachLine { line ->
        outputStream.write(
            line
                .replace(scriptLineReplacePattern, "")
                .replace(linkLine1ReplacePattern, "")
                .replace(linkLine2ReplacePattern, "")
                .replace(metaLine1ReplacePattern, "")
                .replace(metaLine2ReplacePattern, "")
                .replace(commentLineReplacePattern, "")
                .replace("&nbsp;", "")
                .replace("&", "&amp;")
                .run {
                    if (imgLinePattern matches this) {
                        this.replace(imgLineReplacePattern, "<img $1 />")
                    } else {
                        this
                    }
                }
                .run {
                    when {
                        imgBeginPattern matches this -> {
                            insideImg = true
                            this
                        }
                        insideImg -> {
                            val indexOfClose = this.indexOf(">")
                            if (indexOfClose >= 0) {
                                insideImg = false
                                if (this[indexOfClose - 1] != '/') {
                                    this.replaceFirst(">", "/>")
                                } else {
                                    this
                                }
                            } else {
                                this
                            }
                        }
                        else -> this
                    }
                }
                .run {
                    when {
                        scriptBeginPattern matches this -> {
                            insideScript = true
                            replace(scriptBeginReplacePattern, "")
                        }
                        scriptEndPattern matches this -> {
                            insideScript = false
                            replace(scriptEndReplacePattern, "")
                        }
                        insideScript -> ""
                        else -> this
                    }
                }
                .run {
                    openedDiv += countMatches(this, "<div")
                    closedDiv += countMatches(this, "</div>")
                    if (closedDiv > openedDiv) {
                        val times = closedDiv - openedDiv
                        closedDiv -= times
                        removeLast("</div>", times)
                    } else {
                        this
                    }
                }
//                .also {
//                    if (it.isNotEmpty())
//                        Timber.e("Scrapper read -> $it")
//                }
                .toByteArray()
        )
    }

    return ByteArrayInputStream(outputStream.toByteArray())
}

private val scriptLineReplacePattern = "(<script[^>]*>)(.*)(</script>)".toRegex()
private val scriptBeginPattern = "(?:.*)(<script[^>]*>)(.*)".toRegex()
private val scriptBeginReplacePattern = "(<script[^>]*>)(.*)".toRegex()
private val scriptEndPattern = "(.*)(</script>)(?:.*)".toRegex()
private val scriptEndReplacePattern = "(.*)(</script>)".toRegex()
private val linkLine1ReplacePattern = "(<link[^>]*>)(.*)(</link>)".toRegex()
private val linkLine2ReplacePattern = "(<link[^>]*>)".toRegex()
private val metaLine1ReplacePattern = "(<meta[^>]*>)(.*)(</meta>)".toRegex()
private val metaLine2ReplacePattern = "(<meta[^>]*>)".toRegex()
private val commentLineReplacePattern = "(<!--)([^-]*)(-->)".toRegex()
private val imgLinePattern = "(?:.*)(<img[^>]+)(?<!/)>(?:.*)".toRegex()
private val imgLineReplacePattern = "<img\\s+(([a-z]+=\".*?\")+\\s*)>".toRegex()
private val imgBeginPattern = "(?:.*)(<img[^>]*)".toRegex()
