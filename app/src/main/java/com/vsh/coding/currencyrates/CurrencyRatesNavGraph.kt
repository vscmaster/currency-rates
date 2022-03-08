package com.vsh.coding.currencyrates

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vsh.coding.currencyrates.di.ReferencesLocator
import com.vsh.coding.currencyrates.ui.RatesRoute

@Composable
fun CurrencyRatesNavGraph(
    context: Context,
    referencesLocator: ReferencesLocator,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = RouteDestinations.RATES_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(RouteDestinations.RATES_ROUTE) {
            val currencyRatesViewModel: CurrencyRatesViewModel = viewModel(
                factory = CurrencyRatesViewModel.provideFactory(referencesLocator.currencyRepository)
            )
            RatesRoute(
                context = context,
                currencyRatesViewModel = currencyRatesViewModel,
            )
        }
    }
}
