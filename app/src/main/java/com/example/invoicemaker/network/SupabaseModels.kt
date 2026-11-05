package com.example.invoicemaker.network

data class AuthRequest(
    val email: String,
    val password: String
)

data class RefreshRequest(
    val refresh_token: String
)

data class AuthUser(
    val id: String? = null,
    val email: String? = null
)

data class AuthSession(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val expires_at: Long? = null,
    val expires_in: Long? = null,
    val user: AuthUser? = null
)

/**
 * GoTrue returns a session on login, and either a session or a bare user on signup
 * (the bare user happens when email confirmation is still turned on).
 */
data class AuthResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val expires_at: Long? = null,
    val expires_in: Long? = null,
    val user: AuthUser? = null,
    val session: AuthSession? = null,
    val id: String? = null,
    val email: String? = null
)

data class InvoiceRow(
    val id: String? = null,
    val owner_id: String? = null,
    val invoice_number: String? = null,
    val client_name: String? = null,
    val client_email: String? = null,
    val client_address: String? = null,
    val client_phone: String? = null,
    val base_currency: String? = null,
    val display_currency: String? = null,
    val exchange_rate: Double? = null,
    val status: String? = null,
    val notes: String? = null,
    val created_at: String? = null,
    val due_at: String? = null,
    val line_items: List<LineItemRow>? = null
)

data class InvoiceWrite(
    val invoice_number: String,
    val client_name: String,
    val client_email: String,
    val client_address: String,
    val client_phone: String,
    val base_currency: String,
    val display_currency: String,
    val exchange_rate: Double,
    val status: String,
    val notes: String,
    val created_at: String,
    val due_at: String
)

data class LineItemRow(
    val id: String? = null,
    val invoice_id: String? = null,
    val description: String? = null,
    val quantity: Double? = null,
    val unit_price: Double? = null,
    val position: Int? = null
)

data class LineItemWrite(
    val id: String,
    val invoice_id: String,
    val description: String,
    val quantity: Double,
    val unit_price: Double,
    val position: Int
)
