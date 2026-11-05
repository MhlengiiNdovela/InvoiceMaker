package com.example.invoicemaker

import android.app.Application
import com.example.invoicemaker.data.SessionStore

class InvoiceMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionStore.init(this)
    }
}
