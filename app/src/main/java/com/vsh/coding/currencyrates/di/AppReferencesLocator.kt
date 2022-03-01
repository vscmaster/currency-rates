package com.vsh.coding.currencyrates.di

import android.content.Context
import com.vsh.coding.currencyrates.data.CurrencyRatesRepository
import com.vsh.coding.currencyrates.data.CurrencyRepository
import com.vsh.coding.currencyrates.data.api.KtorCurrencyApiBuilder

interface ReferencesLocator {
    val currencyRepository: CurrencyRepository
}

class ReferenceLocatorImpl(private val applicationContext: Context) : ReferencesLocator {

    override val currencyRepository: CurrencyRepository by lazy {
        //CurrencyRatesRepository(RetrofitCurrencyApiBuilder.apiService)
        CurrencyRatesRepository(KtorCurrencyApiBuilder.apiService)
    }
}
