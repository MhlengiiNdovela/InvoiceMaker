package com.example.invoicemaker.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import com.example.invoicemaker.data.model.Client
import com.example.invoicemaker.data.model.Invoice
import com.example.invoicemaker.data.model.LineItem
import java.text.DateFormat
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    viewModel: InvoiceViewModel,
    ownerUid: String,
    onBack: () -> Unit
) {
    val invoice = viewModel.selectedInvoice ?: return

    var invoiceNumber by remember { mutableStateOf(invoice.invoiceNumber) }
    var clientName by remember { mutableStateOf(invoice.client.name) }
    var clientEmail by remember { mutableStateOf(invoice.client.email) }
    var displayCurrency by remember { mutableStateOf(invoice.displayCurrency) }
    var lineItems by remember { mutableStateOf(invoice.lineItems) }
    var status by remember { mutableStateOf(invoice.status) }
    var notes by remember { mutableStateOf(invoice.notes) }

    var convertedTotal by remember { mutableStateOf<Double?>(invoice.convertedTotal) }
    var isConverting by remember { mutableStateOf(false) }
    var emailStatus by remember { mutableStateOf<String?>(null) }

    val baseCurrency = invoice.baseCurrency
    val subtotal = lineItems.sumOf { it.total }

    val context = LocalContext.current
    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            val currentInvoice = Invoice(
                id = invoice.id,
                ownerUid = ownerUid,
                invoiceNumber = invoiceNumber,
                client = Client(name = clientName, email = clientEmail),
                lineItems = lineItems,
                baseCurrency = baseCurrency,
                displayCurrency = displayCurrency,
                exchangeRate = if (subtotal > 0 && convertedTotal != null) convertedTotal!! / subtotal else invoice.exchangeRate,
                status = status,
                notes = notes,
                createdAtMillis = invoice.createdAtMillis,
                dueDateMillis = invoice.dueDateMillis
            )
            generateInvoicePdf(context, it, currentInvoice)
        }
    }

    LaunchedEffect(subtotal, displayCurrency) {
        if (subtotal > 0) {
            isConverting = true
            convertedTotal = viewModel.fetchConvertedAmount(subtotal, baseCurrency, displayCurrency)
            isConverting = false
        } else {
            convertedTotal = 0.0
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Invoice Details") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Invoice number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = clientEmail,
                    onValueChange = { clientEmail = it },
                    label = { Text("Client email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = displayCurrency,
                    onValueChange = { displayCurrency = it.uppercase().take(3) },
                    label = { Text("Invoice currency (e.g. USD, GBP, EUR)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status (DRAFT, SENT, PAID)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(text = "Line items", style = MaterialTheme.typography.titleMedium)
            }

            items(lineItems) { item ->
                LineItemRow(
                    item = item,
                    onChange = { updated ->
                        lineItems = lineItems.map { if (it.id == updated.id) updated else it }
                    },
                    onRemove = {
                        lineItems = lineItems.filterNot { it.id == item.id }
                    }
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        lineItems = lineItems + LineItem(id = UUID.randomUUID().toString())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Add line item")
                }
            }

            item { Divider() }

            item {
                Text(text = "Subtotal ($baseCurrency): %.2f".format(subtotal))
                if (isConverting) {
                    Text(text = "Converting to $displayCurrency...")
                } else {
                    convertedTotal?.let {
                        Text(
                            text = "Total ($displayCurrency): %.2f".format(it),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val updatedInvoice = Invoice(
                            id = invoice.id,
                            ownerUid = ownerUid,
                            invoiceNumber = invoiceNumber,
                            client = Client(name = clientName, email = clientEmail),
                            lineItems = lineItems.filter { it.description.isNotBlank() },
                            baseCurrency = baseCurrency,
                            displayCurrency = displayCurrency,
                            exchangeRate = if (subtotal > 0 && convertedTotal != null) convertedTotal!! / subtotal else 1.0,
                            status = status,
                            notes = notes,
                            createdAtMillis = invoice.createdAtMillis,
                            dueDateMillis = invoice.dueDateMillis
                        )
                        viewModel.saveInvoice(updatedInvoice) { success, error, _ ->
                            if (success) {
                                viewModel.loadInvoices(ownerUid)
                                Toast.makeText(context, "Invoice updated successfully!", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Update failed: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Invoice")
                }
            }

            item {
                Button(
                    onClick = {
                        createPdfLauncher.launch("Invoice_$invoiceNumber.pdf")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF2E7D32))
                ) {
                    Text("Save as PDF")
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(invoice.id, ownerUid)
                        Toast.makeText(context, "Invoice deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Invoice")
                }
            }

            item {
                Button(
                    onClick = {
                        emailStatus = "Sending..."
                        val currentInvoice = Invoice(
                            id = invoice.id,
                            ownerUid = ownerUid,
                            invoiceNumber = invoiceNumber,
                            client = Client(name = clientName, email = clientEmail),
                            lineItems = lineItems,
                            baseCurrency = baseCurrency,
                            displayCurrency = displayCurrency,
                            exchangeRate = if (subtotal > 0 && convertedTotal != null) convertedTotal!! / subtotal else invoice.exchangeRate,
                            status = status,
                            notes = notes
                        )
                        viewModel.sendInvoiceEmail(
                            invoice = currentInvoice,
                            fromEmail = "ST10385107@rcconnect.edu.za"
                        ) { success, error ->
                            emailStatus = if (success) "Invoice emailed to client!" else "Email failed: $error"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Invoice by Email")
                }
            }

            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to List")
                }
            }
        }
    }
}

@Composable
private fun LineItemRow(
    item: LineItem,
    onChange: (LineItem) -> Unit,
    onRemove: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = item.description,
            onValueChange = { onChange(item.copy(description = it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (item.quantity == 0.0) "" else item.quantity.toString(),
                onValueChange = { text ->
                    onChange(item.copy(quantity = text.toDoubleOrNull() ?: 0.0))
                },
                label = { Text("Qty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                onValueChange = { text ->
                    onChange(item.copy(unitPrice = text.toDoubleOrNull() ?: 0.0))
                },
                label = { Text("Unit price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
            Text("Remove")
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

private fun generateInvoicePdf(context: Context, uri: Uri, invoice: Invoice) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.color = Color.BLACK
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("INVOICE", 40f, 60f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    canvas.drawText("Invoice Number: #${invoice.invoiceNumber}", 40f, 100f, paint)
    canvas.drawText("Date: ${DateFormat.getDateInstance().format(Date(invoice.createdAtMillis))}", 40f, 120f, paint)

    paint.isFakeBoldText = true
    canvas.drawText("Billed To:", 40f, 160f, paint)
    paint.isFakeBoldText = false
    canvas.drawText("Name: ${invoice.client.name}", 40f, 180f, paint)
    canvas.drawText("Email: ${invoice.client.email}", 40f, 200f, paint)

    paint.isFakeBoldText = true
    canvas.drawText("Description", 40f, 250f, paint)
    canvas.drawText("Qty", 300f, 250f, paint)
    canvas.drawText("Unit Price", 380f, 250f, paint)
    canvas.drawText("Total", 480f, 250f, paint)

    canvas.drawLine(40f, 260f, 550f, 260f, paint)

    paint.isFakeBoldText = false
    var yPosition = 280f
    invoice.lineItems.forEach { item ->
        if (yPosition < 800f) {
            canvas.drawText(item.description, 40f, yPosition, paint)
            canvas.drawText(item.quantity.toString(), 300f, yPosition, paint)
            canvas.drawText("%.2f".format(item.unitPrice), 380f, yPosition, paint)
            canvas.drawText("%.2f".format(item.total), 480f, yPosition, paint)
            yPosition += 20f
        }
    }

    canvas.drawLine(40f, yPosition, 550f, yPosition, paint)
    yPosition += 25f

    paint.isFakeBoldText = true
    canvas.drawText("Subtotal (${invoice.baseCurrency}): %.2f".format(invoice.subtotal), 40f, yPosition, paint)
    yPosition += 20f
    canvas.drawText("Total (${invoice.displayCurrency}): %.2f".format(invoice.convertedTotal), 40f, yPosition, paint)

    if (invoice.notes.isNotBlank()) {
        yPosition += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Notes:", 40f, yPosition, paint)
        paint.isFakeBoldText = false
        canvas.drawText(invoice.notes, 40f, yPosition + 20f, paint)
    }

    pdfDocument.finishPage(page)

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
            Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}
