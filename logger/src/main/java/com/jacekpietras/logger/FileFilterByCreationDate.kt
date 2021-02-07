package com.jacekpietras.logger

import android.annotation.SuppressLint
import java.io.File
import java.io.FileFilter
import java.util.*
import java.util.regex.Pattern

@SuppressLint("LogNotTimber")
internal class FileFilterByCreationDate(
    private val daysToExpire: Int = 0,
    private val fileNamePattern: Pattern? = null,
    private val getDate: ((fileName: String) -> Date?)? = null,
    private val mode: FileFilterMode = FileFilterMode.STALE,
) : FileFilter {

    override fun accept(file: File): Boolean {
        val prefixMatches = fileNamePattern?.matcher(file.name)?.matches() ?: true
        val expired = when (mode) {
            FileFilterMode.FRESH -> getExpirationDate(file).after(Date())
            FileFilterMode.STALE -> getExpirationDate(file).before(Date())
            FileFilterMode.ALL -> true
        }
        return prefixMatches && expired
    }

    private fun getExpirationDate(file: File): Date {
        val fileCalendar = Calendar.getInstance()
        fileCalendar.time = getCreationDateOfFile(file)
        fileCalendar.add(Calendar.DAY_OF_MONTH, daysToExpire)
        return fileCalendar.time
    }

    private fun getCreationDateOfFile(file: File): Date {
        if (getDate != null) {
            val date = getDate.invoke(file.name)
            if (date != null) return date
        }
        return Date(file.lastModified())
    }

    internal enum class FileFilterMode {
        ALL,
        STALE,
        FRESH,
    }
}