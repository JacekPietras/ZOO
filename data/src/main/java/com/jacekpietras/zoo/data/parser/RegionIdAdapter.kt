package com.jacekpietras.zoo.data.parser

import com.jacekpietras.zoo.domain.model.RegionId
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class RegionIdAdapter {

    @FromJson
    fun fromJson(string: String): RegionId = RegionId(string)

    @ToJson
    fun toJson(value: RegionId) = value.id
}
