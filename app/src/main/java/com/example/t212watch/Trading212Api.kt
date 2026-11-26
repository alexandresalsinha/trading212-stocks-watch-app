package com.example.t212watch

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

data class Position(
    val ticker: String,
    val quantity: Double,
    val averagePrice: Double,
    val currentPrice: Double,
    val ppl: Double,
    val fxPpl: Double?,
) {
    val displayTicker: String get() = ticker.substringBefore("_")
    val value: Double get() = quantity * currentPrice
}

data class AccountCash(
    val free: Double?,
    val invested: Double?,
    val ppl: Double?,
    val result: Double?,
    val total: Double?,
)

interface Trading212Service {
    @GET("api/v0/equity/portfolio")
    suspend fun getPortfolio(): List<Position>

    @GET("api/v0/equity/account/cash")
    suspend fun getAccountCash(): AccountCash
}

object Trading212Api {
    val service: Trading212Service by lazy {
        val basicAuth = Credentials.basic(BuildConfig.T212_API_KEY, BuildConfig.T212_API_SECRET)

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", basicAuth)
                        .build()
                )
            }
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.T212_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(Trading212Service::class.java)
    }
}
