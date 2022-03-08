package com.vsh.coding.currencyrates

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.accompanist.insets.ProvideWindowInsets
import com.vsh.coding.currencyrates.theme.CurrencyRatesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appRefLocator = (application as CurrencyRatesApplication).referenceLocator
        setContent {
            CurrencyRatesTheme {
                ProvideWindowInsets {
                    CurrencyRatesNavGraph(context = this, referencesLocator = appRefLocator)
                }
            }
        }
    }
}