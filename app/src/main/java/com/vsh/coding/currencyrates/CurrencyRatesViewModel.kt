package com.vsh.coding.currencyrates

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vsh.coding.currencyrates.data.ApiResult
import com.vsh.coding.currencyrates.data.CurrencyRepository
import com.vsh.coding.currencyrates.data.model.Rates
import com.vsh.coding.currencyrates.ui.model.CurrenciesRates
import com.vsh.coding.currencyrates.ui.model.MonthRates
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

const val MONTH_3M_FORMAT = "MMM"

private val currenciesIsoCodesTopSet: Set<String> = setOf(
    "CLP",
    "CNY",
    "COP",
    "CRC",
    "CVE",
    "CZK",
    "DJF",
    "DKK",
    "DOP",
    "DZD",
    "EGP",
    "ETB",
    "EUR",
    "FJD"
)

sealed interface CurrencyRatesUiState {

    val currenciesRates: CurrenciesRates?
    val isLoading: Boolean
    val errorMessages: List<ErrorMessage>
    val currentCurrencyRateDate: Date

    data class Empty(
        override val currenciesRates: CurrenciesRates?,
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val currentCurrencyRateDate: Date
    ) : CurrencyRatesUiState

    data class Data(
        override val currenciesRates: CurrenciesRates,
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val currentCurrencyRateDate: Date
    ) : CurrencyRatesUiState
}

private data class CurrencyRatesModelState(
    val rates: List<Rates?> = emptyList(),
    val currentCurrencyRateDate: Date,
    val isLoading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
) {

    fun toUiState(currenciesRatesConverter: (List<Rates?>) -> CurrenciesRates): CurrencyRatesUiState =
        if (rates.isEmpty()) {
            CurrencyRatesUiState.Empty(
                currenciesRates = null,
                isLoading = isLoading,
                errorMessages = errorMessages,
                currentCurrencyRateDate = currentCurrencyRateDate
            )
        } else {
            CurrencyRatesUiState.Data(
                currenciesRates = currenciesRatesConverter(rates),
                isLoading = isLoading,
                errorMessages = errorMessages,
                currentCurrencyRateDate = currentCurrencyRateDate
            )
        }
}

class CurrencyRatesViewModel(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val viewModelState = MutableStateFlow(
        CurrencyRatesModelState(
            isLoading = true,
            currentCurrencyRateDate = Date()
        )
    )

    val uiState = viewModelState
        .map {
            it.toUiState { rates ->
                toCurrenciesRates(rates)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState { rates ->
                toCurrenciesRates(rates)
            }
        )

    init {
        refreshRates()
    }

    fun refreshRates(date: Date = Date()) {
        viewModelState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = currencyRepository.getYearRates(date)
            viewModelState.update {
                when (result) {
                    is ApiResult.Success -> it.copy(
                        rates = result.data,
                        isLoading = false,
                        currentCurrencyRateDate = date
                    )
                    is ApiResult.Error -> {
                        val errorMessages = it.errorMessages + ErrorMessage(
                            id = UUID.randomUUID().mostSignificantBits,
                            messageId = R.string.load_error_check_connection
                        )
                        it.copy(errorMessages = errorMessages, isLoading = false)
                    }
                }
            }
        }
    }

    fun errorShown(errorId: Long) {
        viewModelState.update { currentUiState ->
            val errorMessages = currentUiState.errorMessages.filterNot { it.id == errorId }
            currentUiState.copy(errorMessages = errorMessages)
        }
    }

    private fun toCurrenciesRates(
        rates: List<Rates?>,
    ): CurrenciesRates {
        val currenciesCodes: List<String> = topCurrenciesIsoCodes(rates.first())
        val monthRates: List<MonthRates> = topCurrenciesRates(rates)
        return CurrenciesRates(
            currenciesIsoCodes = currenciesCodes,
            monthRates = monthRates,
        )
    }

    private fun topCurrenciesRates(rates: List<Rates?>): List<MonthRates> {
        return rates.map { rate -> MonthRates(monthName = monthName(rate), rates = topRates(rate)) }
    }

    private fun topRates(rate: Rates?): List<String> {
        if (rate == null) {
            return emptyList()
        }

        return rate.rates.entries.filter { entry -> currenciesIsoCodesTopSet.contains(entry.key) }
            .sortedBy { entry -> entry.key }.map { entry -> normalizeCurrency(entry.value) }
    }

    private fun normalizeCurrency(value: Double): String =
        BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toString()


    private fun monthName(rates: Rates?): String? {
        if (rates == null) {
            return null
        }
        val baseDate = Date(rates.dateMilliseconds)
        return DateFormat.format(MONTH_3M_FORMAT, baseDate).toString()
    }

    private fun topCurrenciesIsoCodes(rates: Rates?): List<String> {
        if (rates == null) {
            return emptyList()
        }
        return rates.rates.keys.filter { key -> currenciesIsoCodesTopSet.contains(key) }.toList()
            .sorted()
    }

    companion object {
        fun provideFactory(
            currencyRatesRepository: CurrencyRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CurrencyRatesViewModel(currencyRatesRepository) as T
            }
        }
    }
}
