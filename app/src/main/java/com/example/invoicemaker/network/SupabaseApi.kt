package com.example.invoicemaker.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApi {
    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body body: AuthRequest
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token")
    suspend fun passwordGrant(
        @Header("apikey") apiKey: String,
        @Query("grant_type") grantType: String,
        @Body body: AuthRequest
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token")
    suspend fun refreshGrant(
        @Header("apikey") apiKey: String,
        @Query("grant_type") grantType: String,
        @Body body: RefreshRequest
    ): Response<AuthResponse>

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String
    ): Response<Void>
}

interface SupabaseInvoiceApi {
    @GET("rest/v1/invoices")
    suspend fun listInvoices(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Query(value = "select", encoded = true) select: String,
        @Query(value = "owner_id", encoded = true) ownerId: String,
        @Query(value = "order", encoded = true) order: String
    ): Response<List<InvoiceRow>>

    @GET("rest/v1/invoices")
    suspend fun getInvoice(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Query(value = "select", encoded = true) select: String,
        @Query(value = "id", encoded = true) id: String
    ): Response<List<InvoiceRow>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/invoices")
    suspend fun createInvoice(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String,
        @Body body: InvoiceWrite
    ): Response<List<InvoiceRow>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/invoices")
    suspend fun updateInvoice(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String,
        @Query(value = "id", encoded = true) id: String,
        @Body body: InvoiceWrite
    ): Response<List<InvoiceRow>>

    @DELETE("rest/v1/invoices")
    suspend fun deleteInvoice(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Query(value = "id", encoded = true) id: String
    ): Response<Void>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/line_items")
    suspend fun upsertLineItems(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Header("Prefer") prefer: String,
        @Query(value = "on_conflict", encoded = true) onConflict: String,
        @Body body: List<LineItemWrite>
    ): Response<Void>

    @DELETE("rest/v1/line_items")
    suspend fun deleteLineItems(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Query(value = "invoice_id", encoded = true) invoiceId: String,
        @Query(value = "id", encoded = true) id: String? = null
    ): Response<Void>
}
