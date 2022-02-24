package com.vsh.coding.currencyrates.ui.model

data class CurrenciesRates(
    val currenciesIsoCodes: List<String>,
    val monthRates: List<MonthRates>,
)
