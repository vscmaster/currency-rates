package com.vsh.coding.currencyrates

import android.app.Application
import com.vsh.coding.currencyrates.di.ReferenceLocatorImpl
import com.vsh.coding.currencyrates.di.ReferencesLocator


class CurrencyRatesApplication : Application() {

    lateinit var referenceLocator: ReferencesLocator

    override fun onCreate() {
        super.onCreate()
        referenceLocator = ReferenceLocatorImpl(this)
    }
}
