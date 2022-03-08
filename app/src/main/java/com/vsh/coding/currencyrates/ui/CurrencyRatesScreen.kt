package com.vsh.coding.currencyrates.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.vsh.coding.currencyrates.CurrencyRatesUiState
import com.vsh.coding.currencyrates.R
import com.vsh.coding.currencyrates.ui.model.CurrenciesRates
import com.vsh.coding.currencyrates.ui.model.MonthRates
import com.vsh.coding.currencyrates.utils.isScrolled
import com.vsh.coding.currencyrates.utils.rateRowColor
import java.util.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CurrencyRateScreen(
    context: Context,
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
        modifier = modifier,
        onRefresh = onYearChange
    ) { ratesUiState, contentModifier ->
        CurrenciesRatesTable(
            context = context,
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
    context: Context,
    currenciesRates: CurrenciesRates,
    modifier: Modifier,
    currentCurrencyRateDate: Date,
    onYearChange: (date: Date) -> Unit
) {
    val scrollState = rememberScrollState()
    val headerHeight = Modifier.height(25.dp)
    Column {
        NavigationBar(context, currentCurrencyRateDate, onYearChange)
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
                currenciesRates.currenciesIsoCodes.forEachIndexed { index, currencyCode ->
                    Text(
                        text = currencyCode,
                        textAlign = TextAlign.Center,
                        color = if (index % 2 == 0) MaterialTheme.colors.onPrimary else Color.Unspecified,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) MaterialTheme.colors.rateRowColor else Color.Transparent)
                            .padding(vertical = 4.dp),
                        fontWeight = FontWeight.Bold
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
private fun CurrenciesRatesScreenWithList(
    uiState: CurrencyRatesUiState,
    showTopAppBar: Boolean,
    homeListLazyListState: LazyListState,
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier,
    onRefresh: (date: Date) -> Unit,
    hasRatesContent: @Composable (
        uiState: CurrencyRatesUiState.Data,
        modifier: Modifier
    ) -> Unit,
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
                        if (uiState.errorMessages.isNotEmpty()) {
                            RetryMessage(uiState, onRefresh)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun RetryMessage(uiState: CurrencyRatesUiState, onRefresh: (date: Date) -> Unit) {
    val errorMessage = remember(uiState) { uiState.errorMessages[0] }

    val errorMessageText: String = stringResource(errorMessage.messageId)
    val retryMessageText = stringResource(id = R.string.retry)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = errorMessageText)
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = { onRefresh(uiState.currentCurrencyRateDate) },
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            Text(text = retryMessageText)
        }
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
        var pages = if (monthsRates.size < columnsPerPage) 1 else monthsRates.size / columnsPerPage

        if (pages * columnsPerPage < monthsRates.size) {
            pages++
        }

        HorizontalPager(count = pages, modifier = modifier) { page ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until columnsPerPage) {
                    val monthRateIndex = page * columnsPerPage + i
                    if (monthRateIndex >= monthsRates.size) break
                    val monthRate = monthsRates[monthRateIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = monthRate.monthName ?: "",
                            modifier = headerHeight,
                            fontWeight = FontWeight.Bold
                        )
                        monthRate.rates.forEachIndexed { index, value ->
                            Text(
                                text = value,
                                textAlign = TextAlign.Center,
                                color = if (index % 2 == 0) MaterialTheme.colors.onPrimary else Color.Unspecified,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (index % 2 == 0) MaterialTheme.colors.rateRowColor else Color.Transparent)
                                    .padding(vertical = 4.dp)
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