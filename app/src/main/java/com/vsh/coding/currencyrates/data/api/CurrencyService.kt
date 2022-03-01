package com.vsh.coding.currencyrates.data.api

import android.util.Log
import com.vsh.coding.currencyrates.BuildConfig
import com.vsh.coding.currencyrates.data.ApiResult
import com.vsh.coding.currencyrates.data.model.Rates
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


private const val BASE_URL = "https://openexchangerates.org/api/"

interface CurrencyService {
    @GET("historical/{date}")
    suspend fun getRatesOfDate(
        @Path("date") date: String,
        @Query("app_id") appId: String = BuildConfig.API_ID
    ): Rates
}

object RetrofitCurrencyApiBuilder {

    private fun getCurrencyService(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: CurrencyService = getCurrencyService().create(CurrencyService::class.java)
}

object KtorCurrencyApiBuilder : CurrencyService {

    private object CustomAndroidHttpLogger : Logger {
        private const val logTag = "CurrencyService"

        override fun log(message: String) {
            Log.i(logTag, message)
        }
    }

    private val client: HttpClient by lazy {
        HttpClient(Android) {
            engine {
                threadsCount = 4
                pipelining = true
            }

            install(Logging) {
                logger = CustomAndroidHttpLogger
                level = LogLevel.INFO
            }

            install(JsonFeature) {
                serializer = GsonSerializer()
            }

            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value
                    //default ktor exception mapping
                    when (statusCode) {
                        in 300..399 -> ApiResult.Error(IllegalArgumentException("Redirect error"))
                        in 400..499 -> ApiResult.Error(IllegalArgumentException("Client request error"))
                        in 500..599 -> ApiResult.Error(IllegalArgumentException("Server response error"))
                    }
                }
            }
        }
    }

    override suspend fun getRatesOfDate(date: String, appId: String): Rates {
        return client.get("${BASE_URL}historical/$date") {
            parameter("app_id", BuildConfig.API_ID)
        }
    }

    val apiService: CurrencyService
        get() = this
}