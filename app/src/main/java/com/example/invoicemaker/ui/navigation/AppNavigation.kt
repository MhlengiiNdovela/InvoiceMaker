package com.example.invoicemaker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.invoicemaker.ui.auth.AuthViewModel
import com.example.invoicemaker.ui.auth.LoginScreen
import com.example.invoicemaker.ui.auth.RegisterScreen
import com.example.invoicemaker.ui.invoice.CreateInvoiceScreen
import com.example.invoicemaker.ui.invoice.InvoiceListScreen
import com.example.invoicemaker.ui.invoice.InvoiceViewModel
import com.example.invoicemaker.ui.invoice.InvoiceDetailScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val INVOICE_LIST = "invoice_list"
    const val CREATE_INVOICE = "create_invoice"
    const val INVOICE_DETAIL = "invoice_detail"
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val authViewModel = remember { AuthViewModel() }
    val invoiceViewModel = remember { InvoiceViewModel() }

    val startDestination = if (authViewModel.isLoggedIn) Routes.INVOICE_LIST else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.INVOICE_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.INVOICE_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.INVOICE_LIST) {
            val uid = authViewModel.currentUserUid ?: return@composable
            InvoiceListScreen(
                viewModel = invoiceViewModel,
                uid = uid,
                onCreateInvoice = { navController.navigate(Routes.CREATE_INVOICE) },
                onOpenInvoice = { invoice ->
                    invoiceViewModel.selectedInvoice = invoice
                    navController.navigate(Routes.INVOICE_DETAIL)
                },
                onSignOut = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.INVOICE_LIST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CREATE_INVOICE) {
            val uid = authViewModel.currentUserUid ?: return@composable
            CreateInvoiceScreen(
                viewModel = invoiceViewModel,
                ownerUid = uid,
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.INVOICE_DETAIL) {
            val uid = authViewModel.currentUserUid ?: return@composable
            InvoiceDetailScreen(
                viewModel = invoiceViewModel,
                ownerUid = uid,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
