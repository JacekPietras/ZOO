package com.jacekpietras.logger

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.util.*
import java.util.regex.Pattern

@OptIn(ObsoleteCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE", "SameParameterValue", "unused")
@SuppressLint("LogNotTimber")
object LogSupport {

    private val timeFormat = ThreadSafeDateFormat("HH:mm:ss.SSS")
    private val dateFormat = ThreadSafeDateFormat("yyyy-MM-dd")
    private var todayFileNameSuffix = dateFormat.format(Date())
    private const val extension = "log"
    private val filePattern =
        { prefix: String -> Pattern.compile("${prefix}-(\\d{4}-\\d{2}-\\d{2})\\.$extension") }

    // Ensures that we work in one and same coroutine,
    // so suspend fun will be called sequentially in proper time order (FIFO)
    private val logMessageActor = CoroutineScope(Dispatchers.Default).actor<LogMessage> {
        for (msg in channel) saveLogInFile(msg)
    }

    fun writeDown(
        logChannel: LogChannel,
        content: String,
        throwable: Throwable? = null,
    ) = CoroutineScope(Dispatchers.IO).launch {
        val fileName = "${logChannel.prefix}-$todayFileNameSuffix.$extension"
        val parentLogsDirectory = getLogsDirectory()

        if (parentLogsDirectory.mkdirAndLog()) {
            val file = File(parentLogsDirectory, fileName)
            val logToSave = "${timeFormat.format(Date())} $content"
            logMessageActor.send(
                LogMessage(
                    logToSave,
                    file,
                    throwable
                )
            )
        }
    }

    fun getLogs(logChannel: LogChannel): List<Pair<Date, String>> {
        val pattern = filePattern(logChannel.prefix)
        return getLogsDirectory()
            .listFiles(
                FileFilterByCreationDate(
                    fileNamePattern = pattern,
                    mode = FileFilterByCreationDate.FileFilterMode.ALL,
                )
            )?.mapNotNull { file ->
                val date = getDate(pattern, file.name)
                val content = file.readText()
                if (date != null) {
                    date to content
                } else {
                    null
                }
            }
            ?: emptyList()
    }

    fun purgeStaleFiles(logChannel: LogChannel) = CoroutineScope(Dispatchers.IO).launch {
        val staleFiles = getLogsDirectory().listFiles(
            FileFilterByCreationDate(
                daysToExpire = logChannel.daysToExpire,
                fileNamePattern = filePattern(logChannel.prefix),
                getDate = { filename -> getDate(filePattern(logChannel.prefix), filename) },
                mode = FileFilterByCreationDate.FileFilterMode.STALE,
            )
        )

        staleFiles?.forEach { file ->
            if (!file.delete()) {
                Log.e("LogSupport", "Unable to delete file with logs: ${file.name}")
            }
        }
    }

    private suspend fun saveLogInFile(log: LogMessage) = withContext(Dispatchers.IO) {
        try {
            val pw = PrintWriter(FileWriter(log.file, true))
            pw.println(log.line)
            log.throwable?.printStackTrace(pw)
            pw.close()
        } catch (e: IOException) {
            Log.e("LogSupport", "Error writing to the log file", e)
        }
    }

    private fun getLogsDirectory() =
        File(DebugUtilsContextHolder.context.getExternalFilesDir(null), "Logs")

    private fun getDate(pattern: Pattern, fileName: String): Date? {
        try {
            val matcher = pattern.matcher(fileName)
            if (matcher.matches()) {
                val datePart = matcher.group(1)
                if (datePart != null) {
                    return dateFormat.parse(datePart)
                }
            }
        } catch (ignored: Exception) {
            // this will never happen, unless someone messes up the regex
            Log.d("FileFilter", "file: $fileName don't have date in name")
        }
        return null
    }
}

private class LogMessage(
    val line: String,
    val file: File,
    val throwable: Throwable?,
)

