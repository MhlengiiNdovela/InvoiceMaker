package com.example.invoicemaker.data.repository

import com.example.invoicemaker.BuildConfig
import com.example.invoicemaker.data.model.Client
import com.example.invoicemaker.data.model.Invoice
import com.example.invoicemaker.data.model.LineItem
import com.example.invoicemaker.network.InvoiceRow
import com.example.invoicemaker.network.InvoiceWrite
import com.example.invoicemaker.network.IsoTimes
import com.example.invoicemaker.network.LineItemWrite
import com.example.invoicemaker.network.RetrofitClient
import com.example.invoicemaker.network.SupabaseGateway
import retrofit2.Response
import java.util.UUID

/**
 * CRUD for invoices through the Supabase REST API.
 * Row level security limits every query to the signed-in user.
 */
class InvoiceRepository {

    suspend fun createInvoice(invoice: Invoice): Result<String> {
        val created = SupabaseGateway.withAccessToken { token ->
            RetrofitClient.supabaseInvoices.createInvoice(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                bearer = SupabaseGateway.bearer(token),
                prefer = SupabaseGateway.returnRepresentation(),
                body = invoice.toWrite()
            )
        }
        val id = created.fold(
            onSuccess = { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception(SupabaseGateway.errorMessage(response)))
                }
                response.body()?.firstOrNull()?.id
            },
            onFailure = { return Result.failure(it) }
        ) ?: return Result.failure(Exception("Invoice was not created"))

        if (invoice.lineItems.isEmpty()) return Result.success(id)
        return replaceLines(id, invoice.lineItems).map { id }
    }

    suspend fun updateInvoice(invoice: Invoice): Result<Unit> {
        if (invoice.id.isBlank()) return Result.failure(Exception("Invoice has no id"))
        val updated = SupabaseGateway.withAccessToken { token ->
            RetrofitClient.supabaseInvoices.updateInvoice(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                bearer = SupabaseGateway.bearer(token),
                prefer = SupabaseGateway.returnRepresentation(),
                id = "eq.${invoice.id}",
                body = invoice.toWrite()
            )
        }
        updated.fold(
            onSuccess = { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception(SupabaseGateway.errorMessage(response)))
                }
            },
            onFailure = { return Result.failure(it) }
        )
        return replaceLines(invoice.id, invoice.lineItems)
    }

    suspend fun deleteInvoice(invoiceId: String): Result<Unit> {
        return execute(
            SupabaseGateway.withAccessToken { token ->
                RetrofitClient.supabaseInvoices.deleteInvoice(
                    apiKey = BuildConfig.SUPABASE_ANON_KEY,
                    bearer = SupabaseGateway.bearer(token),
                    id = "eq.$invoiceId"
                )
            }
        )
    }

    suspend fun getInvoicesForUser(uid: String): Result<List<Invoice>> {
        val result = SupabaseGateway.withAccessToken { token ->
            RetrofitClient.supabaseInvoices.listInvoices(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                bearer = SupabaseGateway.bearer(token),
                select = SupabaseGateway.selectClause(),
                ownerId = "eq.$uid",
                order = "created_at.desc"
            )
        }
        return result.fold(
            onSuccess = { response ->
                if (!response.isSuccessful) {
                    Result.failure(Exception(SupabaseGateway.errorMessage(response)))
                } else {
                    Result.success(response.body().orEmpty().map { it.toInvoice() })
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun getInvoiceById(invoiceId: String): Result<Invoice?> {
        val result = SupabaseGateway.withAccessToken { token ->
            RetrofitClient.supabaseInvoices.getInvoice(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                bearer = SupabaseGateway.bearer(token),
                select = SupabaseGateway.selectClause(),
                id = "eq.$invoiceId"
            )
        }
        return result.fold(
            onSuccess = { response ->
                if (!response.isSuccessful) {
                    Result.failure(Exception(SupabaseGateway.errorMessage(response)))
                } else {
                    Result.success(response.body()?.firstOrNull()?.toInvoice())
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun replaceLines(invoiceId: String, items: List<LineItem>): Result<Unit> {
        val deleted = execute(
            SupabaseGateway.withAccessToken { token ->
                RetrofitClient.supabaseInvoices.deleteLineItems(
                    apiKey = BuildConfig.SUPABASE_ANON_KEY,
                    bearer = SupabaseGateway.bearer(token),
                    invoiceId = "eq.$invoiceId"
                )
            }
        )
        if (deleted.isFailure) return deleted
        if (items.isEmpty()) return Result.success(Unit)

        val body = items.mapIndexed { index, item ->
            LineItemWrite(
                id = item.id.ifBlank { UUID.randomUUID().toString() },
                invoice_id = invoiceId,
                description = item.description,
                quantity = item.quantity,
                unit_price = item.unitPrice,
                position = index
            )
        }
        return execute(
            SupabaseGateway.withAccessToken { token ->
                RetrofitClient.supabaseInvoices.upsertLineItems(
                    apiKey = BuildConfig.SUPABASE_ANON_KEY,
                    bearer = SupabaseGateway.bearer(token),
                    prefer = "resolution=merge-duplicates",
                    onConflict = "id",
                    body = body
                )
            }
        )
    }

    private fun execute(result: Result<Response<Void>>): Result<Unit> {
        return result.fold(
            onSuccess = { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception(SupabaseGateway.errorMessage(response)))
            },
            onFailure = { Result.failure(it) }
        )
    }
}

private fun Invoice.toWrite(): InvoiceWrite {
    return InvoiceWrite(
        invoice_number = invoiceNumber,
        client_name = client.name,
        client_email = client.email,
        client_address = client.address,
        client_phone = client.phone,
        base_currency = baseCurrency,
        display_currency = displayCurrency,
        exchange_rate = exchangeRate,
        status = status,
        notes = notes,
        created_at = IsoTimes.toIso(createdAtMillis),
        due_at = IsoTimes.toIso(dueDateMillis)
    )
}

private fun InvoiceRow.toInvoice(): Invoice {
    val items = line_items.orEmpty()
        .sortedBy { it.position ?: 0 }
        .map { row ->
            LineItem(
                id = row.id.orEmpty(),
                description = row.description.orEmpty(),
                quantity = row.quantity ?: 0.0,
                unitPrice = row.unit_price ?: 0.0
            )
        }
    return Invoice(
        id = id.orEmpty(),
        ownerUid = owner_id.orEmpty(),
        invoiceNumber = invoice_number.orEmpty(),
        client = Client(
            name = client_name.orEmpty(),
            email = client_email.orEmpty(),
            address = client_address.orEmpty(),
            phone = client_phone.orEmpty()
        ),
        lineItems = items,
        baseCurrency = base_currency ?: "ZAR",
        displayCurrency = display_currency ?: "ZAR",
        exchangeRate = exchange_rate ?: 1.0,
        createdAtMillis = created_at?.let(IsoTimes::toMillis) ?: System.currentTimeMillis(),
        dueDateMillis = due_at?.let(IsoTimes::toMillis) ?: System.currentTimeMillis(),
        status = status ?: "DRAFT",
        notes = notes.orEmpty()
    )
}
