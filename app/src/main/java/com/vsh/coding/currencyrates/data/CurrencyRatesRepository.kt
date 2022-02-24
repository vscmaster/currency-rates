package com.vsh.coding.currencyrates.data

import android.util.Log
import com.vsh.coding.currencyrates.data.api.CurrencyService
import com.vsh.coding.currencyrates.data.model.Rates
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

private const val API_DATE_FORMAT = "yyyy-MM-dd"
private const val API_DATE_FORMAT_SUFFIX = ".json"

class CurrencyRatesRepository(private val currencyService: CurrencyService) : CurrencyRepository {

    private val apiDateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
    }

    override suspend fun getYearRates(date: Date): ApiResult<List<Rates>> {

        val ratesQueue = ConcurrentLinkedQueue<Rates>()
        coroutineScope {
            val requestDate: Calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val monthsCount = monthsCountOfYear(requestDate)
            for (i in 0 until monthsCount) {
                requestDate.set(Calendar.MONTH, i)

                withContext(Dispatchers.IO + errorHandler) {
                    val dateFormatParameter = apiDateFormat.format(requestDate.time)
                    val dateString = dateFormatParameter + API_DATE_FORMAT_SUFFIX
                    delay((500L..1000L).random())
                    val result: Result<Rates> =
                        handleRequest { currencyService.getRatesOfDate(dateString) }
                    ratesQueue.add(result.getOrNull())
                }
            }
        }

        return if (ratesQueue.isEmpty()) {
            ApiResult.Error(IllegalArgumentException("Rates not found"))
        } else {
            ApiResult.Success(ratesQueue.toList())
        }
    }

    private val errorHandler = CoroutineExceptionHandler { context, exception ->
        run {
            println("Caught $exception")
            Log.e("CurrencyRatesRepository", "unable to download rates", exception)
        }
    }

    private fun monthsCountOfYear(requestDate: Calendar): Int {
        val currentDate: Calendar = Calendar.getInstance()
        val rYear = requestDate.get(Calendar.YEAR)
        val cYear = currentDate.get(Calendar.YEAR)
        val cMonth = currentDate.get(Calendar.MONTH)

        return if (rYear < cYear) 12 else cMonth + 1
    }

    private suspend fun <T : Any> handleRequest(requestFunc: suspend () -> T): Result<T> {
        return try {
            Result.success(requestFunc.invoke())
        } catch (he: HttpException) {
            Log.e("CurrencyRatesRepository", "unable to download rates", he)
            Result.failure(he)

        }
    }
}
