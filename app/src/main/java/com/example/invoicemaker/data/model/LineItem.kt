package com.example.invoicemaker.data.model

/**
 * A single billable line on an invoice, e.g. "Web design services x 3 @ 150.00".
 */
data class LineItem(
    val id: String = "",
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0
) {
    val total: Double
        get() = quantity * unitPrice
}
