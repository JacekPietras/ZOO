package com.jacekpietras.zoo.core.text

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.strikeThrough
import androidx.core.text.underline

sealed class Text {

    data class Value(val stringValue: CharSequence) : Text()

    data class Res(
        @StringRes val stringRes: Int, val formatArgs: List<Any>
    ) : Text() {

        constructor(
            @StringRes stringRes: Int,
            vararg formatArgs: Any,
        ) : this(stringRes, formatArgs.toList())
    }

    data class Listing(val texts: List<Text>, val separator: Text = COMMA_SEPARATOR) : Text() {

        constructor() : this(emptyList())

        constructor(vararg texts: Text) : this(texts.toList())

        constructor(@StringRes vararg texts: Int) : this(texts.toList().map { Res(it) })
    }

    data class Concat(
        val texts: List<Text>,
    ) : Text() {

        constructor(vararg texts: Text) : this(texts.toList())
    }

    data class Plural(
        @PluralsRes val pluralRes: Int,
        val quantity: Int,
        val formatArgs: List<Any>,
    ) : Text() {

        constructor(
            @PluralsRes pluralRes: Int,
            quantity: Int,
            vararg formatArgs: Any,
        ) : this(pluralRes, quantity, formatArgs.toList())
    }

    data class Colored(
        @AttrRes val attrColor: Int,
        val text: Text,
    ) : Text()

    data class Underline(
        val text: Text,
    ) : Text()

    data class StrikeThrough(
        val text: Text,
    ) : Text()

    data class Bold(
        val text: Text,
    ) : Text()

    object Empty : Text()

    companion object {
        val COMMA_SEPARATOR = Value(", ")

        operator fun CharSequence.plus(right: Text): Text =
            when (right) {
                is Empty -> Value(this)
                is Concat -> right.copy(texts = listOf(Value(this)) + right.texts)
                else -> Concat(texts = listOf(Value(this), right))
            }

        operator fun invoke(value: String): Text = Value(value)

        operator fun invoke(@StringRes stringRes: Int): Text = Res(stringRes)
    }

    fun toCharSeq(context: Context): CharSequence {
        return when (this) {
            is Value -> stringValue
            is Res -> {
                if (formatArgs.isEmpty()) {
                    context.getString(stringRes)
                } else {
                    @Suppress("SpreadOperator")
                    context.getString(stringRes, *formatArgs.textsToString(context).toTypedArray())
                }
            }
            is Listing -> {
                val separator = separator.toCharSeq(context)
                buildSpannedString {
                    texts
                        .filterNot { it is Empty }
                        .forEachIndexed { i, text ->
                            if (i > 0) append(separator)
                            append(text.toString(context))
                        }
                }
            }
            is Concat -> {
                buildSpannedString {
                    texts.forEach { append(it.toCharSeq(context)) }
                }
            }
            is Underline -> {
                buildSpannedString {
                    underline {
                        append(text.toCharSeq(context))
                    }
                }
            }
            is StrikeThrough -> {
                buildSpannedString {
                    strikeThrough {
                        append(text.toCharSeq(context))
                    }
                }
            }
            is Bold -> {
                buildSpannedString {
                    bold {
                        append(text.toCharSeq(context))
                    }
                }
            }
            is Plural -> {
                if (formatArgs.isEmpty()) {
                    context.resources.getQuantityString(pluralRes, quantity)
                } else {
                    @Suppress("SpreadOperator")
                    context.resources.getQuantityString(
                        pluralRes,
                        quantity,
                        *formatArgs.toTypedArray()
                    )
                }
            }
            is Colored -> {
                buildSpannedString {
                    color(context.getColorFromAttribute(attrColor)) {
                        append(text.toCharSeq(context))
                    }
                }
            }
            Empty -> ""
        }
    }

    fun toString(context: Context): String =
        toCharSeq(context).toString()

    operator fun plus(right: Text): Text =
        when {
            this is Empty && right is Empty -> Empty
            this is Empty -> right
            right is Empty -> this
            this is Concat -> this.copy(texts = texts + right)
            right is Concat -> right.copy(texts = listOf(this) + right.texts)
            else -> Concat(texts = listOf(this, right))
        }

    operator fun plus(right: CharSequence): Text =
        when (this) {
            is Empty -> Value(right)
            is Concat -> this.copy(texts = texts + Value(right))
            else -> Concat(texts = listOf(this, Value(right)))
        }

    fun strikeThrough(): Text =
        StrikeThrough(this)

    fun underline(): Text =
        Underline(this)

    fun bold(): Text =
        Bold(this)
}

private fun List<Any>.textsToString(context: Context) =
    map {
        if (it is Text) {
            it.toCharSeq(context)
        } else {
            it
        }
    }

fun Context.getColorFromAttribute(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}
