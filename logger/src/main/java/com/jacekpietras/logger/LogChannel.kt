package com.jacekpietras.logger

interface LogChannel {
    val prefix: String
    val daysToExpire: Int
}
