package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.models.PaymentResult
import io.pleo.antaeus.models.Invoice

interface PaymentService {

    fun payInvoice(invoice : Invoice) : PaymentResult

}