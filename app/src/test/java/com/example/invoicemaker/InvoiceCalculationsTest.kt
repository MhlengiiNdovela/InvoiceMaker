package com.example.invoicemaker

import com.example.invoicemaker.data.model.Invoice
import com.example.invoicemaker.data.model.LineItem
import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceCalculationsTest {

    @Test
    fun `line item total is quantity times unit price`() {
        val item = LineItem(description = "Consulting", quantity = 3.0, unitPrice = 150.0)
        assertEquals(450.0, item.total, 0.001)
    }

    @Test
    fun `invoice subtotal sums all line items`() {
        val invoice = Invoice(
            lineItems = listOf(
                LineItem(description = "Design", quantity = 2.0, unitPrice = 100.0),
                LineItem(description = "Hosting", quantity = 1.0, unitPrice = 50.0)
            )
        )
        assertEquals(250.0, invoice.subtotal, 0.001)
    }

    @Test
    fun `converted total applies exchange rate to subtotal`() {
        val invoice = Invoice(
            lineItems = listOf(LineItem(description = "Service", quantity = 1.0, unitPrice = 1000.0)),
            baseCurrency = "ZAR",
            displayCurrency = "USD",
            exchangeRate = 0.055
        )
        assertEquals(55.0, invoice.convertedTotal, 0.001)
    }

    @Test
    fun `same currency conversion is a no-op`() {
        val invoice = Invoice(
            lineItems = listOf(LineItem(description = "Service", quantity = 1.0, unitPrice = 200.0)),
            baseCurrency = "ZAR",
            displayCurrency = "ZAR",
            exchangeRate = 1.0
        )
        assertEquals(200.0, invoice.convertedTotal, 0.001)
    }
}
