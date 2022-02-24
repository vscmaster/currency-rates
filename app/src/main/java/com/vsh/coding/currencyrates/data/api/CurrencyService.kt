package com.vsh.coding.currencyrates.data.api

import com.vsh.coding.currencyrates.BuildConfig
import com.vsh.coding.currencyrates.data.model.Rates
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


object CurrencyApiBuilder {

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