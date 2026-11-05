package com.example.invoicemaker.data.model

/**
 * Represents a client/customer that an invoice is billed to.
 */
data class Client(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val phone: String = ""
)
