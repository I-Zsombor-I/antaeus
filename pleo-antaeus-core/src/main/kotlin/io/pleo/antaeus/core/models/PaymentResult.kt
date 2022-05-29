package io.pleo.antaeus.core.models

import io.pleo.antaeus.models.Invoice

data class PaymentResult (
        val status: PaymentStatus,
        val invoice: Invoice
)