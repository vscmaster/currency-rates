package com.vsh.coding.currencyrates

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object RouteDestinations {
    const val RATES_ROUTE = "rates"
}


class CurrencyRatesNavigationActions(navController: NavHostController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(RouteDestinations.RATES_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
