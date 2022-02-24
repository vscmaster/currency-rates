package com.vsh.coding.currencyrates.data

import com.vsh.coding.currencyrates.data.model.Rates
import java.util.*

interface CurrencyRepository {

    suspend fun getYearRates(date: Date): ApiResult<List<Rates>>
}
