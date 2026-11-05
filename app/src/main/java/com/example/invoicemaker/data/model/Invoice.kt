package com.example.invoicemaker.data.model

/**
 * An invoice, owned by the authenticated user (ownerUid), billed to a Client,
 * made up of LineItems, in a base currency (ZAR by default) with an optional
 * converted total in a different display currency (via the exchange rate API).
 */
data class Invoice(
    val id: String = "",
    val ownerUid: String = "",
    val invoiceNumber: String = "",
    val client: Client = Client(),
    val lineItems: List<LineItem> = emptyList(),
    val baseCurrency: String = "ZAR",
    val displayCurrency: String = "ZAR",
    val exchangeRate: Double = 1.0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val dueDateMillis: Long = System.currentTimeMillis(),
    val status: String = "DRAFT", // DRAFT, SENT, PAID
    val notes: String = ""
) {
    val subtotal: Double
        get() = lineItems.sumOf { it.total }

    /** Subtotal converted into displayCurrency using exchangeRate. */
    val convertedTotal: Double
        get() = subtotal * exchangeRate
}
