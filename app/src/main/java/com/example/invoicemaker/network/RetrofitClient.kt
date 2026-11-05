package com.example.invoicemaker.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Single place that builds and exposes our Retrofit-backed API clients.
 * Two different base URLs -> two different Retrofit instances.
 */
object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // --- Exchange Rate API ---
    private val exchangeRateRetrofit = Retrofit.Builder()
        .baseUrl("https://v6.exchangerate-api.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val exchangeRateApi: ExchangeRateApi by lazy {
        exchangeRateRetrofit.create(ExchangeRateApi::class.java)
    }

    // --- SendGrid Email API ---
    private val emailRetrofit = Retrofit.Builder()
        .baseUrl("https://api.sendgrid.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val emailApi: EmailApi by lazy {
        emailRetrofit.create(EmailApi::class.java)
    }
}
