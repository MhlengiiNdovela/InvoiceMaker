package com.example.invoicemaker.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * External REST API: exchangerate-api.com
 * Sign up for a free key at https://www.exchangerate-api.com/
 *
 * This satisfies the "use a RESTful API" and "external library" requirements -
 * we call it via Retrofit rather than hand-rolling HTTP requests.
 */
interface ExchangeRateApi {

    @GET("v6/{apiKey}/latest/{baseCurrency}")
    suspend fun getLatestRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String
    ): ExchangeRateResponse
}
