package com.vsh.coding.currencyrates.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.vsh.coding.currencyrates.CurrencyRatesUiState
import com.vsh.coding.currencyrates.R
import com.vsh.coding.currencyrates.ui.model.CurrenciesRates
import com.vsh.coding.currencyrates.ui.model.MonthRates
import com.vsh.coding.currencyrates.utils.isScrolled
import java.util.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CurrencyRateScreen(
    uiState: CurrencyRatesUiState,
    showTopAppBar: Boolean,
    homeListLazyListState: LazyListState,
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier,
    onYearChange: (date: Date) -> Unit
) {
    CurrenciesRatesScreenWithList(
        uiState = uiState,
        showTopAppBar = showTopAppBar,
        homeListLazyListState = homeListLazyListState,
        scaffoldState = scaffoldState,
        modifier = modifier
    ) { ratesUiState, contentModifier ->
        CurrenciesRatesTable(
            currenciesRates = ratesUiState.currenciesRates,
            modifier = contentModifier,
            currentCurrencyRateDate = ratesUiState.currentCurrencyRateDate,
            onYearChange = onYearChange
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CurrenciesRatesTable(
    currenciesRates: CurrenciesRates,
    modifier: Modifier,
    currentCurrencyRateDate: Date,
    onYearChange: (date: Date) -> Unit
) {
    val scrollState = rememberScrollState()
    val headerHeight = Modifier.height(25.dp)
    Column {
        YearNavigation(currentCurrencyRateDate, onYearChange)
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(40.dp)
            ) {
                Box(modifier = Modifier.height(25.dp))
                currenciesRates.currenciesIsoCodes.forEach { currencyCode ->
                    Text(
                        text = currencyCode,
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .width(1.dp)
                    )
                }
            }
            RatesPagerList(
                modifier = modifier,
                monthsRates = currenciesRates.monthRates,
                headerHeight = headerHeight
            )
        }
    }
}

@Composable
private fun YearNavigation(currentRateDate: Date, onYearChange: (date: Date) -> Unit) {
    val currentRateDateCalendar: Calendar = Calendar.getInstance().apply { time = currentRateDate }
    val currentRateYear = currentRateDateCalendar.get(Calendar.YEAR)
    var currentRateYearState by rememberSaveable { mutableStateOf(currentRateYear) }

    Row(
        modifier = Modifier
            .background(color = colorResource(id = R.color.purple_200))
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(modifier = Modifier.padding(start = 5.dp),
            onClick = {
                val newValue = backYear(currentRateYearState)
                currentRateYearState = newValue
                if (newValue != currentRateYear) {
                    onYearChange(currentRateDateCalendar.apply {
                        set(
                            Calendar.YEAR,
                            newValue
                        )
                    }.time)
                }
            }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_move_year_back)
            )
        }
        Text(
            text = "$currentRateYearState", modifier = Modifier
                .weight(1f), textAlign = TextAlign.Center
        )
        IconButton(modifier = Modifier.padding(start = 5.dp),
            onClick = {
                val newValue = forwardYear(currentRateYearState)
                currentRateYearState = newValue
                if (newValue != currentRateYear) {
                    onYearChange(currentRateDateCalendar.apply {
                        set(
                            Calendar.YEAR,
                            newValue
                        )
                    }.time)
                }
            }) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = stringResource(R.string.cd_move_year_forward)
            )
        }
    }
}

private fun backYear(currentRateYear: Int): Int {
    return if (currentRateYear > 2000) currentRateYear - 1 else currentRateYear
}

private fun forwardYear(currentRateYear: Int): Int {
    val currentDateCalendar = Calendar.getInstance()
    val currentYear = currentDateCalendar.get(Calendar.YEAR)

    return if (currentRateYear < currentYear) currentRateYear + 1 else currentRateYear
}

@Composable
private fun CurrenciesRatesScreenWithList(
    uiState: CurrencyRatesUiState,
    showTopAppBar: Boolean,
    homeListLazyListState: LazyListState,
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier,
    hasRatesContent: @Composable (
        uiState: CurrencyRatesUiState.Data,
        modifier: Modifier
    ) -> Unit
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (showTopAppBar) {
                TopAppBar(
                    elevation = if (!homeListLazyListState.isScrolled) 0.dp else 4.dp,
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)

        LoadingContent(
            empty = when (uiState) {
                is CurrencyRatesUiState.Data -> false
                is CurrencyRatesUiState.Empty -> uiState.isLoading
            },
            emptyContent = { FullScreenLoading() },
            loading = uiState.isLoading,
            content = {
                when (uiState) {
                    is CurrencyRatesUiState.Data -> hasRatesContent(uiState, contentModifier)
                    is CurrencyRatesUiState.Empty -> {

                    }
                }
            }
        )
    }
}

@Composable
private fun LoadingContent(
    empty: Boolean,
    emptyContent: @Composable () -> Unit,
    loading: Boolean,
    content: @Composable () -> Unit
) {
    if (empty) {
        emptyContent()
    } else {
        content()
        if (loading) {
            FullScreenLoading()
        }
    }
}

@ExperimentalPagerApi
@Composable
private fun RatesPagerList(
    monthsRates: List<MonthRates>,
    modifier: Modifier = Modifier,
    headerHeight: Modifier,
) {

    if (monthsRates.isNotEmpty()) {
        val configuration = LocalConfiguration.current
        val columnsPerPage = when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> 6
            else -> 4
        }
        val pages = if (monthsRates.size < columnsPerPage) 1 else monthsRates.size / columnsPerPage

        HorizontalPager(count = pages, modifier = modifier) { page ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until columnsPerPage) {
                    val monthRateIndex = page * columnsPerPage + i
                    if (monthRateIndex >= monthsRates.size) break
                    val monthRate = monthsRates[monthRateIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = monthRate.monthName ?: "",
                            modifier = headerHeight,
                            fontWeight = FontWeight.Bold
                        )
                        monthRate.rates.forEach { value ->
                            Text(text = value, modifier = Modifier.padding(vertical = 4.dp))
                            Divider(
                                color = Color.LightGray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .width(1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenLoading() {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Blue.copy(alpha = 0.1f))
            .clickable(interactionSource = interactionSource, indication = null) {},
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    }
}

@Composable
private fun TopAppBar(
    elevation: Dp,
) {
    val title = stringResource(id = R.string.app_name)
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 4.dp, top = 10.dp)
            )
        },
        //backgroundColor = MaterialTheme.colors.onPrimary,
        elevation = elevation
    )
}

@Composable
fun rememberContentPaddingForScreen(additionalTop: Dp = 0.dp) =
    rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyTop = false,
        applyEnd = false,
        applyStart = false,
        additionalTop = additionalTop
    )