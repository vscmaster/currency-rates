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
private const val TAG = "CurrencyRatesRepository"


class CurrencyRatesRepository(private val currencyService: CurrencyService) : CurrencyRepository {

    private val apiDateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
    }

    override suspend fun getYearRates(date: Date): ApiResult<List<Rates>> {

        val ratesQueue = ConcurrentLinkedQueue<Rates>()
        //val parallelScope = CoroutineScope(Job() + errorHandler)
        val result = kotlin.runCatching {
            //parallelScope.launch(SupervisorJob()) {
            val requestDate: Calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val monthsCount = monthsCountOfYear(requestDate)
            withContext(Dispatchers.IO) {
                for (i in 0 until monthsCount) {
                    val requestMonth: Calendar = Calendar.getInstance().apply {
                        time = requestDate.time
                        requestDate.set(Calendar.MONTH, i)
                    }

                    launch {
                        val dateFormatParameter = apiDateFormat.format(requestMonth.time)
                        val dateString = dateFormatParameter + API_DATE_FORMAT_SUFFIX
                        delay((1000L..2000L).random())
                        val result: Rates =
                            currencyService.getRatesOfDate(dateString)
                        Log.d(TAG, "Thread: ${Thread.currentThread().name} has finished!")
                        ratesQueue.add(result)
                    }
                }
            }
        }

        if (result.isFailure) {
            Log.e("CurrencyRatesRepository", "unable to download rates", result.exceptionOrNull())
            return ApiResult.Error(IllegalArgumentException("Rates not found"))
        }

        return if (ratesQueue.isEmpty()) {
            ApiResult.Error(IllegalArgumentException("Rates not found"))
        } else {
            ApiResult.Success(ratesQueue.toList())
        }
    }

    private fun monthsCountOfYear(requestDate: Calendar): Int {
        val currentDate: Calendar = Calendar.getInstance()
        val rYear = requestDate.get(Calendar.YEAR)
        val cYear = currentDate.get(Calendar.YEAR)
        val cMonth = currentDate.get(Calendar.MONTH)

        return if (rYear < cYear) 12 else cMonth + 1
    }

    /*val errorHandler = CoroutineExceptionHandler { context, exception ->
        run {
            Log.e("CurrencyRatesRepository", "unable to download rates", exception)
        }
    }*/
}
