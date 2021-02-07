package com.jacekpietras.zoo

import com.jacekpietras.logger.LogChannel

enum class FileLogChannel(
    override val prefix: String,
    override val daysToExpire: Int,
) : LogChannel {

    GPS("gps", 7),
    TIMBER_WARN("warn", 7),
    TIMBER_ERROR("error", 7),
}