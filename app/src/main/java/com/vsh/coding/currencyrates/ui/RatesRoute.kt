package com.vsh.coding.currencyrates.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import com.vsh.coding.currencyrates.CurrencyRatesViewModel

@Composable
fun RatesRoute(
    context: Context,
    currencyRatesViewModel: CurrencyRatesViewModel,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) {
    val listLazyListState = rememberLazyListState()
    val uiState by currencyRatesViewModel.uiState.collectAsState()
    val isShowAppBar = doesNeedShowAppBar()
    CurrencyRateScreen(
        context = context,
        uiState = uiState,
        showTopAppBar = isShowAppBar,
        scaffoldState = scaffoldState,
        homeListLazyListState = listLazyListState,
        onYearChange = { date -> currencyRatesViewModel.refreshRates(date) }
    )
}

@Composable
fun doesNeedShowAppBar(): Boolean {
    val configuration = LocalConfiguration.current
    return when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> false
        else -> true
    }
}

