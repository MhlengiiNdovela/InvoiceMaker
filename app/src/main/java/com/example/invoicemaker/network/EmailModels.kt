package com.example.invoicemaker.network

/**
 * Request body shapes for SendGrid's v3 Mail Send API:
 * https://docs.sendgrid.com/api-reference/mail-send/mail-send
 *
 * SendGrid expects a fairly nested JSON structure - these data classes
 * mirror that shape so Gson can serialize it directly.
 */
data class SendGridEmailRequest(
    val personalizations: List<Personalization>,
    val from: EmailAddress,
    val subject: String,
    val content: List<EmailContent>
)

data class Personalization(
    val to: List<EmailAddress>
)

data class EmailAddress(
    val email: String,
    val name: String? = null
)

data class EmailContent(
    val type: String = "text/plain",
    val value: String
)
