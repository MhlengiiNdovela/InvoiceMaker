package com.example.invoicemaker.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * External REST API: SendGrid (https://sendgrid.com/)
 * Used to email a generated invoice to a client.
 *
 * Note: SendGrid returns 202 Accepted with an empty body on success,
 * so the return type is a plain Response<Unit>.
 */
interface EmailApi {

    @POST("v3/mail/send")
    suspend fun sendEmail(
        @Header("Authorization") bearerToken: String, // "Bearer YOUR_API_KEY"
        @Body request: SendGridEmailRequest
    ): Response<Unit>
}
