package com.example.invoicemaker.ui.invoice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.invoicemaker.data.model.Invoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    viewModel: InvoiceViewModel,
    uid: String,
    onCreateInvoice: () -> Unit,
    onOpenInvoice: (Invoice) -> Unit,
    onSignOut: () -> Unit
) {
    LaunchedEffect(uid) {
        viewModel.loadInvoices(uid)
    }

    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Invoices") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateInvoice) {
                Icon(Icons.Default.Add, contentDescription = "Create invoice")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                is InvoiceUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
                else -> {
                    if (viewModel.invoices.isEmpty()) {
                        Text(
                            text = "No invoices yet. Tap + to create one.",
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn {
                            items(viewModel.invoices) { invoice ->
                                InvoiceRow(invoice = invoice, onClick = { onOpenInvoice(invoice) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(invoice: Invoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Invoice #${invoice.invoiceNumber}", style = MaterialTheme.typography.titleMedium)
            Text(text = invoice.client.name)
            Text(
                text = "%.2f %s".format(invoice.convertedTotal, invoice.displayCurrency),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = "Status: ${invoice.status}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
