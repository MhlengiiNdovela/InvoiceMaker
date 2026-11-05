package com.example.invoicemaker.network

/**
 * Response shape for exchangerate-api.com's "latest" endpoint:
 * https://v6.exchangerate-api.com/v6/{API_KEY}/latest/{BASE_CODE}
 */
data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)
