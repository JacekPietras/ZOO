package com.jacekpietras.zoo.data.database.utils

import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> T.toJson(): String = Gson().toJson(this, object : TypeToken<T>() {}.type)

inline fun <reified T> String.fromJson(): T = Gson().fromJson(this, object : TypeToken<T>() {}.type)

fun SupportSQLiteDatabase.getTableNames(): MutableList<String> {
    val cursor = query(
        """
            SELECT name FROM sqlite_master
            WHERE type='table' AND name NOT IN (
                'android_metadata',
                'sqlite_sequence',
                'room_master_table'
            )
        """.trimIndent()
    )
    val tableNames = mutableListOf<String>()
    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {
            tableNames.add(cursor.getString(0))
            cursor.moveToNext()
        }
    }
    cursor.close()
    return tableNames
}
