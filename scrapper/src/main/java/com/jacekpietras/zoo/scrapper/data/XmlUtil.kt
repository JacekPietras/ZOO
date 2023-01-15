package com.jacekpietras.zoo.scrapper.data

import android.content.Context
import android.content.res.XmlResourceParser
import androidx.annotation.RawRes
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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

internal fun InputStream.cleanupHtml(print: Boolean = false): InputStream {
    val outputStream = ByteArrayOutputStream()
    var insideScript = false
    var insideImg = false
    var insideP = false
    var openedDiv = 0
    var closedDiv = 0

    bufferedReader().forEachLine { line ->
        outputStream.write(
            line
                .replace(docTypeLineReplacePattern, "")
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
                    if (insideP && this.contains("<") && !this.contains("</p>")) {
                        insideP = false
                        "</p>$this <!-- added p -->"
                    } else if (!this.contains("</p>") && pBeginPattern matches this) {
                        Timber.e("Scrapper read -> <!-- started at: $this -->")
                        insideP = true
                        "$this <!-- started p -->"
                    } else if (insideP && this.contains("</p>")) {
                        insideP = false
                        "$this <!-- finished p -->"
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
                .also {
                    if (print && it.isNotEmpty())
                        Timber.e("Scrapper read -> $it")
                }
                .toByteArray()
        )
    }

    return ByteArrayInputStream(outputStream.toByteArray())
}

fun XmlPullParser.skip() {
    var deep = 0
    while (true) {
        when (eventType) {
            XmlPullParser.START_TAG -> deep++
            XmlPullParser.END_TAG -> deep--
        }
        if (deep == 0) return
        next()
    }
}

@Suppress("unused")
fun makeStreamFromFile(context: Context, @RawRes rawRes: Int) =
    context.resources.openRawResource(rawRes).cleanupHtml()

fun makeStreamFromUrl(url: String, print: Boolean = false) =
    (URL(url).openConnection() as HttpURLConnection)
        .apply {
            setRequestProperty("User-Agent", "")
            requestMethod = "POST"
            doInput = true
            connect()
        }
        .inputStream
        .cleanupHtml(print)

fun difference(str1: String, str2: String): String {
    val at = indexOfDifference(str1, str2)
    return if (at == -1) ""
    else str2.substring(at)
}

private fun indexOfDifference(str1: String, str2: String): Int {
    if (str1 === str2) return -1

    var i = 0
    while (i < str1.length && i < str2.length) {
        if (str1[i] != str2[i]) {
            break
        }
        ++i
    }
    return if (i < str2.length || i < str1.length) i
    else -1
}

fun diffCount(a: String, b: String) =
    diff(a, b).run { (first + second).length }

fun diff(a: String, b: String): Pair<String, String> =
    diffHelper(a, b, HashMap())

private fun diffHelper(
    a: String,
    b: String,
    lookup: MutableMap<Long, Pair<String, String>>
): Pair<String, String> {
    val key = a.length.toLong() shl 32 or b.length.toLong()
    if (!lookup.containsKey(key)) {
        lookup[key] = if (a.isEmpty() || b.isEmpty()) {
            Pair(a, b)
        } else if (a[0] == b[0]) {
            diffHelper(a.substring(1), b.substring(1), lookup)
        } else {
            val aa = diffHelper(a.substring(1), b, lookup)
            val bb = diffHelper(a, b.substring(1), lookup)
            if (aa.first.length + aa.second.length < bb.first.length + bb.second.length) {
                Pair(a[0] + aa.first, aa.second)
            } else {
                Pair(bb.first, b[0] + bb.second)
            }
        }
    }
    return lookup[key]!!
}

private val docTypeLineReplacePattern = "<!(doctype|DOCTYPE)[^>]*>".toRegex()
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
private val pBeginPattern = "(?:.*)(<p)(>| [^/>]+>)(?:.*)".toRegex()
