package com.example.invoicemaker.network

import com.example.invoicemaker.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Single place that builds and exposes our Retrofit-backed API clients.
 */
object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val supabaseClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val supabaseRetrofit: Retrofit by lazy {
        val raw = BuildConfig.SUPABASE_URL.trim()
        val baseUrl = if (raw.startsWith("http")) raw.trimEnd('/') + "/" else "http://localhost/"
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(supabaseClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val supabaseAuth: SupabaseAuthApi by lazy {
        supabaseRetrofit.create(SupabaseAuthApi::class.java)
    }

    val supabaseInvoices: SupabaseInvoiceApi by lazy {
        supabaseRetrofit.create(SupabaseInvoiceApi::class.java)
    }

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
