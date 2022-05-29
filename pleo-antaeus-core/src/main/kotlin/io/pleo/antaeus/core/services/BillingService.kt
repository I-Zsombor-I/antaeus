package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.models.PaymentResult
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
        private val invoiceService: InvoiceService,
        private val paymentService: PaymentService,
        private val summaryService: SummaryService
) {

    fun payPendingInvoices(): List<PaymentResult> {
        val pendingInvoices = invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING)
        logger.info("Billing started for ${pendingInvoices.size} invoices.")

        val paymentResults = pendingInvoices.map(paymentService::payInvoice)

        summaryService.updateSummary(paymentResults)
        logger.info("Billing finished for ${pendingInvoices.size} invoices.")

        return paymentResults
    }

}
