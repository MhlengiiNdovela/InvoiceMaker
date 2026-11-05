package com.example.invoicemaker.data.repository

import com.example.invoicemaker.data.model.Invoice
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * CRUD access to the "invoices" collection in Cloud Firestore.
 * Every invoice is tagged with ownerUid so a user only ever queries their own data.
 */
class InvoiceRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val invoicesCollection = db.collection("invoices")

    suspend fun createInvoice(invoice: Invoice): Result<String> {
        return try {
            val docRef = invoicesCollection.document()
            val invoiceWithId = invoice.copy(id = docRef.id)
            docRef.set(invoiceWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInvoice(invoice: Invoice): Result<Unit> {
        return try {
            invoicesCollection.document(invoice.id).set(invoice).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInvoice(invoiceId: String): Result<Unit> {
        return try {
            invoicesCollection.document(invoiceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvoicesForUser(uid: String): Result<List<Invoice>> {
        return try {
            val snapshot = invoicesCollection
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()
            val invoices = snapshot.documents.mapNotNull { it.toObject(Invoice::class.java) }
            Result.success(invoices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvoiceById(invoiceId: String): Result<Invoice?> {
        return try {
            val doc = invoicesCollection.document(invoiceId).get().await()
            Result.success(doc.toObject(Invoice::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
