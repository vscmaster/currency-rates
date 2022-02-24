package com.vsh.coding.currencyrates.data.model

import com.google.gson.annotations.SerializedName

data class Rates(
    @SerializedName("timestamp")
    val dateSeconds: Int,
    @SerializedName("base")
    val baseISOCode: String,
    val rates: Map<String, Double> = emptyMap(),
) {
    val dateMilliseconds: Long get() = dateSeconds.toLong() * 1000
}
